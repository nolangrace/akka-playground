package com.example

import akka.actor.ActorSystem
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}

object StreamDSLTest extends App {

  val topHeadSink = Sink.ignore
  val bottomHeadSink = Sink.ignore
  val sharedDoubler = Flow[Int].map(x => {
    println(x)
    x * 2
  })

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  RunnableGraph.fromGraph(GraphDSL.create(topHeadSink, bottomHeadSink, sharedDoubler)((_, _, _)) { implicit builder =>
  (topHS, bottomHS, doubler) =>
    import GraphDSL.Implicits._
    val broadcast = builder.add(Broadcast[Int](2))

//    val doubler = builder.add(sharedDoubler)
    Source(1 to 1000) ~> broadcast.in

    broadcast ~> doubler ~> topHS.in
    broadcast ~> doubler ~> bottomHS.in
    ClosedShape
  }).run()

}
