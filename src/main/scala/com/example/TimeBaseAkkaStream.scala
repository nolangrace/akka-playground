package com.example

import java.util.Calendar

import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration._

object TimeBaseAkkaStream extends App {

  implicit val system = ActorSystem("my-system")
  implicit val ec = system.dispatcher

  def isPrime(i: Int): Boolean =
    if (i <= 1)
      false
    else if (i == 2)
      true
    else
      !(2 until i).exists(n => i % n == 0)

  Source.repeat("Element")
    .throttle(100, 1.second)
    .map(x => System.currentTimeMillis())
    .mapAsync(2)(startTime => {
      Future {
        (1 to 100000).map{ x =>
          isPrime(x)
        }

        startTime
      }
    })
    .map(startTime => {
      val end = System.currentTimeMillis()
      end - startTime
    })
    .to(Sink.foreach(println))
    .run()

}
