package com.example

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{MergeHub, RunnableGraph, Sink, Source}

import scala.concurrent.duration._

object AkkaMergeHubExample extends App {

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  // A simple consumer that will print to the console for now
  val consumer = Sink.foreach(println)

  // Attach a MergeHub Source to the consumer. This will materialize to a
  // corresponding Sink.
  val runnableGraph: RunnableGraph[Sink[Int, NotUsed]] =
  MergeHub.source[Int](perProducerBufferSize = 16).to(consumer)

  // By running/materializing the consumer we get back a Sink, and hence
  // now have access to feed elements into it. This Sink can be materialized
  // any number of times, and every element that enters the Sink will
  // be consumed by our consumer.
  val toConsumer: Sink[Int, NotUsed] = runnableGraph.run()



  Source(1 to 1000)
    .throttle(20, 1.second)
    .map(x => {
      val actor = system.actorOf(MergeHubTestActor.props(toConsumer))
      actor ! x
    })
    .runWith(Sink.ignore)

}

class MergeHubTestActor(toConsumer: Sink[Int, NotUsed]) extends Actor {
  implicit val ec = context.dispatcher
  implicit val sys = context.system


  override def receive: Receive = {
    case x:Int => {
      Source.single(x).runWith(toConsumer)
      context.stop(self)
      }
    }

}

object MergeHubTestActor {
  def props(toConsumer: Sink[Int, NotUsed]): Props = Props(new MergeHubTestActor(toConsumer))
}