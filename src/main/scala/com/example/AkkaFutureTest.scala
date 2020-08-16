//package com.example
//
//import akka.NotUsed
//import akka.actor.{ActorSystem, Props}
//import akka.stream.{FlowShape, KillSwitches, Materializer, OverflowStrategy}
//import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Merge, Partition, Sink, Source}
//
//import scala.concurrent.{Await, Future, Promise}
//import scala.concurrent.duration._
//import scala.util.Try
//
////#main-class
//object AkkaFutureTest extends App {
//
//  implicit val system = ActorSystem("mySystem")
//  implicit val ec = system.dispatcher
//
//  val flow = Flow.fromGraph(GraphDSL.create() { implicit b =>
//    import GraphDSL.Implicits._
//
//    val workerCount = 4
//
//    val partition = b.add(Partition[Int](workerCount, _ % workerCount))
//    val merge = b.add(Merge[Int](workerCount))
//
//    for (_ <- 1 to workerCount) {
//      partition ~> Flow[Int].map(x => {
//        delay(10)
//        x
//      }).async ~> merge
//    }
//
//    FlowShape(partition.in, merge.out)
//  })
//
//  val (killSwitch, last) = Source(1 to 10000)
//    .via(flow)
//    .viaMat(KillSwitches.single)(Keep.right)
//    .toMat(Sink.foreach(println))(Keep.both)
//    .run()
//
////  killSwitch.shutdown()
//
//
//
//  def delay(num:Int) = {
//    Thread.sleep(5000)
//  }
//
//
//}