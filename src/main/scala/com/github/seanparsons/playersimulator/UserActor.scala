package com.github.seanparsons.playersimulator

import scala.math._
import java.lang.Thread.sleep
import actors.Scheduler
import akka.util.duration._
import akka.actor.{PoisonPill, Actor}
import java.io.Closeable
import util.Random

case object PlayGame

case object UserActor {
  val waitTimes = (5000 to 10000 by 1000).toArray
  val random = new Random
  def randomWaitTime() = waitTimes(random.nextInt(waitTimes.size - 1))
}

case class UserActor() extends Actor {
  import context._

  protected def waitForABit() {
    val sleepTime = UserActor.randomWaitTime().milliseconds
    // "Sleeps" by getting the ActorSystem to send another PlayGame message to this in a while.
    system.scheduler.scheduleOnce(sleepTime, self, PlayGame)
  }

  protected def receive = {
    // One time only message receipt to burn in the user this actor represents.
    case userProgress: UserProgress => {
      become{
        case PlayGame => {
          // Pretend to make some progress.
          val score = ((random * 150) + 100).toInt
          //println("User %s scored %s.".format(userProgress.userID, score))
          userProgress.progress = min(userProgress.progress + score, Game.totalPerUser)

          // Wait for a bit if the user hasn't "completed" the game.
          if (userProgress.progress < Game.totalPerUser) {
            waitForABit()
          } else {
            //println("User %s has finished.".format(userProgress.userID))
            self ! PoisonPill
          }
        }
      }
      waitForABit()
    }
  }
}
