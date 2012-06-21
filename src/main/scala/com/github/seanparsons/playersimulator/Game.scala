package com.github.seanparsons.playersimulator

import org.lwjgl.opengl.GL11._
import org.lwjgl.util.glu.GLU._
import org.lwjgl.opengl._
import org.lwjgl.input.Keyboard
import Keyboard._
import scalaz._
import Scalaz._
import scalaz.effects._
import com.github.seanparsons.playersimulator.GLHelper._
import akka.actor.{Props, ActorSystem}

case class UserProgress(userID: Int, var progress: Int)

case class GameState(totals: Seq[UserProgress], timePassed: Long)

case class Vector(x: Float, y: Float, z: Float)

case class Side(points: Seq[Vector], normal: Vector)

object Game extends App {
  val screenWidth = 1024
  val screenHeight = 768

  val userFactor = 80
  val totalNumberOfUsers = userFactor * userFactor
  val totalPerUser = 1000
  val spacing = 20.0f
  val columnWidth = 30.0f
  val screenTranslate = (spacing + columnWidth) * userFactor / 2 * -1
  val displayMode = new DisplayMode(screenWidth, screenHeight)
  val pix = new PixelFormat(Display.getDisplayMode().getBitsPerPixel(),
    8,   // alpha
    24,   // depth buffer
    1,   // stencil buffer
    0)

  Display.setTitle("Simulating %s Users".format(totalNumberOfUsers))
  Display.setDisplayMode(displayMode)
  Display.create(pix)

  glDisable(GL_CULL_FACE)
  glMatrixMode(GL_PROJECTION)
  glLoadIdentity()
  gluPerspective(90, screenWidth.toFloat / screenHeight.toFloat, 0.01f, 100000)
  glMatrixMode(GL_MODELVIEW)
  glShadeModel(GL_SMOOTH)
  glEnable(GL_LIGHTING)
  glEnable(GL_LIGHT0)
  glEnable(GL_DEPTH_TEST)
  glEnable(GL_COLOR_MATERIAL)
  glHint(GL_POLYGON_SMOOTH_HINT, GL_FASTEST)

  def next(gameState: GameState, time: Long): GameState = gameState.copy(timePassed = gameState.timePassed + time)
  
  def draw(gameState: GameState): IO[Unit] = {
    io {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

      val wholeGameComplete = !gameState.totals.exists(_.progress.toFloat != totalPerUser)

      glPushPop{
        val angle = (gameState.timePassed.toFloat / 100f) % 360f
        glTranslatef(0, 0, -500)

        glRotatef(-45, 1, 1, 0)

        glRotatef(angle, 0, 0, 1)
        glTranslatef(screenTranslate, screenTranslate, 0)

        gameState.totals.view.zipWithIndex.foreach{case (progress: UserProgress, position: Int) =>
          val gridXPosition = (position % userFactor) * (spacing + columnWidth)
          val gridYPosition = (position / userFactor) * (spacing + columnWidth)
          val topEdge = progress.progress.toFloat / 10f

          glVertices(GLQuads){
            if (wholeGameComplete) {
              glColor3f(0.0f, 1.0f, 0.0f)
            } else if (progress.progress == totalPerUser) {
              glColor3f(0.0f, 0.0f, 1.0f)
            } else {
              glColor3f(1.0f, 0.0f, 0.0f)
            }

            // Bottom
            glNormal3f(0, 0, -1)
            glVertex3f(gridXPosition, gridYPosition, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, 0)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, 0)

            // Top
            glNormal3f(0, 0, 1)
            glVertex3f(gridXPosition, gridYPosition, topEdge)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, topEdge)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, topEdge)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, topEdge)

            // Sides:
            glNormal3f(0, -1, 0)
            glVertex3f(gridXPosition, gridYPosition, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, topEdge)
            glVertex3f(gridXPosition, gridYPosition, topEdge)

            glNormal3f(-1, 0, 0)
            glVertex3f(gridXPosition, gridYPosition, 0)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, 0)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, topEdge)
            glVertex3f(gridXPosition, gridYPosition, topEdge)

            glNormal3f(0, 1, 0)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, topEdge)
            glVertex3f(gridXPosition, gridYPosition + columnWidth, topEdge)

            glNormal3f(1, 0, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, 0)
            glVertex3f(gridXPosition + columnWidth, gridYPosition + columnWidth, topEdge)
            glVertex3f(gridXPosition + columnWidth, gridYPosition, topEdge)
          }
        }
      }
      //glTranslatef(-400, 0, 0)
      Display.update()
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val userProgressElements = (0 until totalNumberOfUsers).map(number => new UserProgress(number, 0))
  val userActors = userProgressElements.map(userProgress => actorSystem.actorOf(Props[UserActor]))
  userActors.zip(userProgressElements).foreach{pair =>
    pair._1 ! pair._2
  }

  new GameLoop().loop(priorGameState = GameState(userProgressElements, 0), action = next, draw = draw)
  
  Display.destroy()
  actorSystem.shutdown()
  sys.exit(0)  
}
