package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.snapshot.MaterializerState
import com.example.TimeBaseAkkaStream.isPrime

import scala.concurrent.Future
import scala.concurrent.duration._

object AkkaStreamsMaterializerState extends App {

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  val flow = Flow[Long]
    .mapAsync(2)(startTime => {
      Future {
        (1 to 100000).map{ x =>
          isPrime(x)
        }

        startTime
      }
    })

  Source.repeat("Element")
    .throttle(100, 1.second)
    .map(x => System.currentTimeMillis())
    .via(flow)
    .map(startTime => {
      val end = System.currentTimeMillis()
      end - startTime
    })
    .to(Sink.ignore).run()

  Source(1 to 10).groupBy(3, _ % 3).mergeSubstreamsWithParallelism(2).to(Sink.ignore).run()

  Source(1 to 10)
    .groupBy(3, _ % 3)
    .map(x => {
      x
    })
    .groupBy(10, _ % 10)
    .map(x => {
      x+1
    })
    .map(x => {
      "bla"
    })
    .map(x => {
      10
    })
    .to(Sink.ignore)
    .run()

  MaterializerState.streamSnapshots(system).onComplete(streamSnap => {
    streamSnap.map(interpreters => {
      interpreters.map(x => {
        x.activeInterpreters.map(y =>{
          y.logics.map( z => {
            z
            println("logic Label: "+z.label)

          })

          y.connections.map( a => {
            println("in to: "+a.in+" out from:"+a.out)
          })
        })
      })
    })
  })

}
