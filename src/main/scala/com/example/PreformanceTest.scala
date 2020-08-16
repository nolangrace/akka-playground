package com.example

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case object Ping
case object Pong

object PreformanceTest extends App {
    val t = new Tester
    t.run()
}

class Tester {
  val REPETITIONS: Int = 100000000

  val startTime: Long = System.nanoTime
  val pongValue: AtomicInteger = new AtomicInteger(0)

  def run() = {
    val system = ActorSystem("PingPongSystem")
    val pongActor = system.actorOf(Props(new PongActor(REPETITIONS, pongValue)), name = "Pong")
    val pingActor = system.actorOf(Props(new PingActor(REPETITIONS)), name = "Ping")

    system.registerOnTermination {
      val duration: Long = System.nanoTime - startTime
      printf("duration %,d (ns)%n", duration)
      printf("%,d ns/op%n", duration / (REPETITIONS * 2L))
      printf("%,d ops/s%n", (REPETITIONS * 2L * 1000000000L) / duration)
      println("pongValue = " + pongValue)
    }

    pongActor.tell(Ping, pingActor)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}

class PingActor(repetitions: Int) extends Actor {

  override def receive = {
    case Pong =>
      sender ! Ping
  }
}

class PongActor(repetitions: Int, pongValue: AtomicInteger) extends Actor {
  var counter: Int = 0

  override def receive = {
    case Ping =>
      counter = counter + 1
      if (counter >= repetitions) {
        pongValue.set(counter)
        context.system.terminate()
      } else {
        sender ! Pong
      }
  }
}

