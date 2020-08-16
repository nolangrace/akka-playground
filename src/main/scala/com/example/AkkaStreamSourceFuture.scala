package com.example

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source, SourceWithContext}
import akka.stream.scaladsl.Source.queue

import scala.concurrent.Future

object AkkaStreamSourceFuture extends App {

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  Option()

  val source:SourceWithContext[Int, Int, Future[NotUsed]] = Source.futureSource(Future {
      Source(1 to 100)
  }).asSourceWithContext(x => x)

  source.to(Sink.foreach(println)).run()
//  val sink: Sink[Int, Future[Done]] = Sink.foreach((i: Int) => println(i))
//
//  val bla = source.map(x => {
//    x.to(sink).run()
//  }).runWith(Sink.head)
//
//  bla.onComplete(println)
}
