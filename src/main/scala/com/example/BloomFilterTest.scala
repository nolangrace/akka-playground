package com.example

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Sink, Source}
import bloomfilter.mutable.BloomFilter

object BloomFilterTest extends App {

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  val testSize = 20000000

  val actor = system.actorOf(BloomFilterActor.props(testSize))

//  val actor = system.actorOf(SetActor.props(testSize))

//  val actor = system.actorOf(EmptyActor.props(testSize))
}

case class Message(id:String, existsInSet:Boolean)

class BloomFilterActor(testSize:Int) extends Actor {
  implicit val mat = Materializer(context)

  Source(1 to testSize)
    .map(x => {
      val message = Message(UUID.randomUUID().toString, false)
      self ! message
    })
    .runWith(Sink.ignore)

  // Create a Bloom filter
  val falsePositiveRate = 0.1
  val bf = BloomFilter[String](testSize, falsePositiveRate)

  var count = 0
  var falsePositiveCount = 0
  var positivePositiveCount = 0

  val startTime = System.currentTimeMillis()

  override def receive: Receive = {
    case m:Message => {
      // Check whether an element in a set
      if(bf.mightContain(m.id)){
        if(m.existsInSet)
          positivePositiveCount += 1
        else
          falsePositiveCount += 1
      } else {
        bf.add(m.id)
      }

      count += 1

      if(count % 1000000 == 0){
        println("Test Size: "+count)
      }

      if(count == testSize) {
        val testMillis = System.currentTimeMillis()-startTime
        val testRate:Double = count.toDouble/(testMillis/1000)

        val falsePositiveMeasuredRate:Double = falsePositiveCount.toDouble/count.toDouble
        println("Test Done!")
        println("Test Size: "+count)
        println("False Positive Rate: "+falsePositiveMeasuredRate)
        println("False Positive Count: "+falsePositiveCount)
        println("Test Rate: "+testRate+" messages/ Second")

        val mb = 1024*1024
        val runtime = Runtime.getRuntime
        println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb +" mb")

        context.stop(self)
      }

    }
  }

  override def postStop() = {
    bf.dispose()
  }
}

object BloomFilterActor {
  def props(testSize: Int): Props = Props(new BloomFilterActor(testSize))
}

class SetActor(testSize:Int) extends Actor {
  implicit val mat = Materializer(context)

  Source(1 to testSize)
    .map(x => {
      val message = Message(UUID.randomUUID().toString, false)
      self ! message
    })
    .runWith(Sink.ignore)

  var members = Set[String]()

  var count = 0

  var duplicateCount = 0

  val startTime = System.currentTimeMillis()

  override def receive: Receive = {
    case m:Message => {
      // Check whether an element in a set
      if(members contains m.id){
        duplicateCount += 1
      } else {
        members = members + m.id
        count += 1
      }



      if(count % 1000000 == 0){
        println("Test Size: "+count)
      }

      if(count == testSize) {
        val testMillis = System.currentTimeMillis()-startTime
        val testRate:Double = count.toDouble/(testMillis/1000)

        println("Test Done!")
        println("Test Size: "+count)
        println("Duplicate Count: "+duplicateCount)
        println("Test Rate: "+testRate+" messages/ Second")

        val mb = 1024*1024
        val runtime = Runtime.getRuntime
        println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb+" mb")

        context.stop(self)
      }

    }
  }
}

object SetActor {
  def props(testSize: Int): Props = Props(new SetActor(testSize))
}

class EmptyActor(testSize:Int) extends Actor {
  implicit val mat = Materializer(context)

  Source(1 to testSize)
    .map(x => {
      val message = Message(UUID.randomUUID().toString, false)
      self ! message
    })
    .runWith(Sink.ignore)

  var count = 0

  val startTime = System.currentTimeMillis()

  override def receive: Receive = {
    case m:Message => {
      count += 1

      if(count % 1000000 == 0){
        println("Test Size: "+count)
      }

      if(count == testSize) {
        val testMillis = System.currentTimeMillis()-startTime
        val testRate:Double = count.toDouble/(testMillis/1000)

        println("Test Done!")
        println("Test Size: "+count)
        println("Test Rate: "+testRate+" messages/ Second")
        context.stop(self)
      }

    }
  }
}

object EmptyActor {
  def props(testSize: Int): Props = Props(new EmptyActor(testSize))
}