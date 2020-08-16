//package com.example
//
//import akka.actor.ActorRef
//import akka.stream.OverflowStrategy
//import akka.stream.scaladsl.{Sink, Source}
//
//object IntToLongTest extends App{
//
//  val stream = Source.queue[Int](
//    1000,
//    OverflowStrategy.backpressure
//  ).map(x => {Thread.sleep(100000000)
//    x
//  }).to(Sink.foreach(println))
//    .run()
//
//}
