//#full-example
package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, FlowWithContext, RetryFlow, Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

//#main-class
object AkkaStreamRestart extends App {

  implicit val system = ActorSystem("mySystem")
  implicit val ec = system.dispatcher

  val elementsToProcess = 100

  implicit val timeout:Timeout = 3.seconds

  def flow = FlowWithContext[Int, Int].mapAsync(1)(msg => {
      Future {
        val rand = Math.floor(Math.random() * 10)

        if (rand % 5 == 0) {
          println("Error: " + rand + " Num: " + msg)
          throw new RuntimeException
        } else {
          msg
        }
      } recover {
        case e: RuntimeException => {
          e
        }
      }
    })

//  val retryFlow:FlowWithContext[Int, Int, Any, Int, NotUsed] =
//    RetryFlow.withBackoffAndContext(
//      minBackoff = 10.millis,
//      maxBackoff = 5.seconds,
//      randomFactor = 0d,
//      maxRetries = 3,
//      flow)(decideRetry = {
//      case ((x, y), (result:RuntimeException, ctx)) => Some(x -> (ctx+1))
//      case _                       => None
//    })

  val errorFlow = Flow[Any]
    .map(x => println("Error Flow: "+x))
    .to(Sink.ignore)

  Source(1 to 100)
    .throttle(elementsToProcess, 1.second)
    .asSourceWithContext(td ⇒ 0)
    .via(flow)
    .map(msg => {msg})
    .asSource
    .divertTo(errorFlow, _._2 >= 1)
    .runWith(Sink.foreach(println))

}