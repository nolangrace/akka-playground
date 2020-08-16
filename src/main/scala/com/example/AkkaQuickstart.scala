////#full-example
//package com.example
//
//
//import akka.actor.{Actor, ActorSystem, Props, Stash, StashOverflowException}
//import akka.stream.{OverflowStrategy, QueueOfferResult}
//import akka.stream.scaladsl.{Keep, Sink, Source}
//
//import scala.concurrent.duration._
//import scala.concurrent.{Await, Future, Promise}
//import scala.util.{Failure, Success, Try}
//import akka.pattern.{pipe, ask}
//import akka.util.Timeout
//
//import scala.concurrent.duration._
//import scala.collection.mutable.ListBuffer
//
////#main-class
//object AkkaQuickstart extends App {
//
//  implicit val system = ActorSystem("mySystem")
//  implicit val ec = system.dispatcher
//
//  val actor = system.actorOf(Props[TestActor])
//
//  val elementsToProcess = 100
//
//  implicit val timeout:Timeout = 3.seconds
//
//  Source(1 to 10000000)
//    .throttle(elementsToProcess, 1.second)
//    .map(actor)
//    .map(msg => {actor ! msg})
//    .runWith(Sink.ignore)
//
//  Source(List(1, 2, 3))
//    .mapAsyncUnordered(10)(msg => {
//      (actor ? msg).mapTo[Ack]
//    })
//    .to(Sink.ignore)
//
//
//
//}
//
//
//class TestActor extends Actor with Stash{
//  implicit val ec = context.dispatcher
//  implicit val sys = context.system
//
//  val bufferSize = 1
//
//  val queue = Source
//    .queue[Int](bufferSize, OverflowStrategy.dropNew)
//    .mapAsync(1)(x=>{
//      Future {
//        Thread.sleep(2000)
//        x
//      }
//    })
//    .toMat(Sink.foreach(println))(Keep.left)
//    .run()
//
//
//
//  override def receive: Receive = {
//    case x:Int => {
//        queue.offer(x).onComplete {
//          case Success(x) => x match {
//            case QueueOfferResult.Enqueued => println(s"enqueued $x")
//            case QueueOfferResult.Dropped => println(s"dropped $x")
//            case QueueOfferResult.Failure(ex) => println(s"Offer failed ${ex.getMessage}")
//            case QueueOfferResult.QueueClosed => println("Source Queue closed")
//          }
//          case Failure(t) => println("An error has occurred: " + t.getMessage)
//        }
//    }
//  }
//}
//
//case class Ack()
//
////class TestActor extends Actor with Stash{
////  implicit val ec = context.dispatcher
////  implicit val sys = context.system
////
////  val bufferSize = 10
////
////  var waitingForStreamSpace = false
////
////  var actorMessageBuffer = new ListBuffer[Int]()
////
////  var listMaxLength = 50
////
////  val queue = Source
////    .queue[Int](bufferSize, OverflowStrategy.backpressure)
////    .mapAsync(10)(x=>{
////      Future {
////        Thread.sleep(2000)
////        x
////      }
////    })
////    .toMat(Sink.foreach(x => println(s"completed $x")))(Keep.left)
////    .run()
////
////
////
////  override def receive: Receive = {
////    case x:Int => {
//////      println("Actor received message: "+x)
////      if(waitingForStreamSpace){
////        if(actorMessageBuffer.size < 1000) {
////          actorMessageBuffer.append(x)
////          println("Buffer Size: "+actorMessageBuffer.size)
////        }else{
////          println("Actor Buffer Full!!!!!")
////        }
////      } else {
////        waitingForStreamSpace = true
////        queue.offer(x).pipeTo(self)
////      }
////    }
////    case QueueOfferResult.Enqueued  => {
//////      println("Enqueue Success")
////      waitingForStreamSpace = false
////      if(actorMessageBuffer.size>0) {
////        val num = actorMessageBuffer(0)
////
////        queue.offer(num).pipeTo(self)
////        actorMessageBuffer.remove(0)
////      }
////    }
////    case QueueOfferResult.Dropped     => {
////      waitingForStreamSpace = false
////      println(s"dropped")
////    }
////    case QueueOfferResult.Failure(ex) => {
////      waitingForStreamSpace = false
////      println(s"Offer failed ${ex.getMessage}")
////    }
////    case QueueOfferResult.QueueClosed => {
////      waitingForStreamSpace = false
////      println("Source Queue closed")
////    }
////  }
////}
////#main-class
////#full-example
