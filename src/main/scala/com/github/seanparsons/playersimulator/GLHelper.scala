package com.github.seanparsons.playersimulator

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._

sealed abstract class GLVerticesMode(val glConstant: Int)
case object GLPoints extends GLVerticesMode(GL_POINTS)
case object GLLines extends GLVerticesMode(GL_LINES) 
case object GLLineStrip extends GLVerticesMode(GL_LINE_STRIP) 
case object GLLineLoop extends GLVerticesMode(GL_LINE_LOOP) 
case object GLTriangles extends GLVerticesMode(GL_TRIANGLES) 
case object GLTriangleStrip extends GLVerticesMode(GL_TRIANGLE_STRIP) 
case object GLTriangleFan extends GLVerticesMode(GL_TRIANGLE_FAN) 
case object GLQuads extends GLVerticesMode(GL_QUADS)
case object GLQuadStrip extends GLVerticesMode(GL_QUAD_STRIP)
case object GLPolygon extends GLVerticesMode(GL_POLYGON)


object GLHelper {
  @inline
  def glVertices(mode: GLVerticesMode)(vertices: => Unit) = {
    GL11.glBegin(mode.glConstant)
    vertices
    GL11.glEnd()
  }

  @inline
  def glPushPop(glCalls: => Unit) = {
    GL11.glPushMatrix()
    glCalls
    GL11.glPopMatrix()
  }
}