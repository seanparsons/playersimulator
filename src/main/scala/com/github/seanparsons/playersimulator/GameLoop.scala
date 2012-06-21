package com.github.seanparsons.playersimulator

import scala.annotation.tailrec
import org.lwjgl.opengl.Display
import org.lwjgl.Sys
import scalaz._
import Scalaz._
import scalaz.effects._

final case class GameLoop(targetFPS: Long = 60) {
  val targetFrameTime: Long = (1000.0 / targetFPS.toDouble).round

  @tailrec
  final def loop[T](priorRun: Long = Sys.getTime(),
                    priorGameState: T,
                    action: (T, Long) => T,
                    draw: (T) => IO[Unit],
                    continue: () => Boolean = () => !Display.isCloseRequested): Unit = {
    if (continue()) {
      // Draw the game state as it is.
      draw(priorGameState).unsafePerformIO
      // Capture the time now.
      val now = Sys.getTime()
      val timeTaken = math.max(now - priorRun, 0)
      // Transform the game state.
      val latestGameState = action(priorGameState, timeTaken)
      // Figure out if we need to pause for any period of time.
      val timeTakenAfterGraphics = Sys.getTime() - priorRun
      if (timeTakenAfterGraphics < targetFrameTime && timeTakenAfterGraphics > 0) {
        Thread.sleep(targetFrameTime - timeTakenAfterGraphics)
      }
      // Keep on looping.
      loop(now, latestGameState, action, draw, continue)
    }
  }
}