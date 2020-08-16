package com.example

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.cluster.typed.{Cluster, Join}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{MergeHub, Sink, Source, SourceQueueWithComplete}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}

object ActorAndMergeHub extends App {

  val config = ConfigFactory.load()
  val customConf = ConfigFactory.parseString("""
  akka.actor.provider = "cluster"
""")

  val system = ActorSystem[Nothing](Guardian(), "bla", ConfigFactory.load(customConf).withFallback(config))


}

object Guardian {
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system = context.system
      val cluster = Cluster(context.system)
      cluster.manager ! Join(cluster.selfMember.address)
      val sharding = ClusterSharding(context.system)

      val TypeKey = EntityTypeKey[Counter.Command]("Counter")

      val streamEnd = MergeHub.source[String](perProducerBufferSize = 16)
        .to(Sink.foreach(println(_))).run()

      val shardRegion: ActorRef[ShardingEnvelope[Counter.Command]] =
        sharding.init(Entity(TypeKey)(createBehavior = entityContext => Counter(entityContext.entityId, streamEnd)))

      Source.repeat("bla")
        .throttle(10, 1.second)
        .map(x => {
          shardRegion ! ShardingEnvelope("counter-"+Random.nextInt(10), Counter.Increment)
        })
        .to(Sink.ignore)
        .run()

      Behaviors.same
    }
  }
}

object Counter {
  sealed trait Command
  case object Increment extends Command
  case object Enqueued extends Command
  case object EnqueuedFailure extends Command
  final case class GetValue(replyTo: ActorRef[Int]) extends Command

  def apply(entityId: String, streamEnd:Sink[String, NotUsed]): Behavior[Command] = Behaviors.receive { (context, message) =>
    def updated(value: Int, stream:SourceQueueWithComplete[String], state:String): Behavior[Command] = {
      Behaviors.withStash(100) { buffer =>
        Behaviors.receiveMessage[Command] {
          case Increment =>
            val newValue = value + 1
            if (state == "waiting") {
              buffer.stash(Increment)
            }
            else if (state == "clear") {
              context.pipeToSelf(stream.offer("Entity: "+entityId+" Count: "+newValue)) {
                case Success(_) => Enqueued
                case Failure(e) => EnqueuedFailure
              }
            }
            updated(newValue, stream, "waiting")
          case GetValue(replyTo) =>
            replyTo ! value
            Behaviors.same
          case Enqueued =>
            updated(value, stream, "clear")
          case EnqueuedFailure =>
            println("Enqueue Failure")
            Behaviors.same
        }
      }
    }

    implicit val sys = context.system
    val offerStream = Source.queue[String](10, OverflowStrategy.backpressure)
      .to(streamEnd).run()

    updated(0, offerStream, "clear")

  }
}
