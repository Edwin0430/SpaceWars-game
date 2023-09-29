import scalafx.Includes.jfxPoint2D2sfx
import scalafx.geometry.Point2D
import scalafx.scene.shape.Circle

class Projectile(
                  initialPosition: Point2D,
                  val direction: Point2D,
                  val shooter: Ship,
                  var radius: Double
                ) {
  private val speed = 10.0
  var position: Point2D = initialPosition

  // Create a Circle instance to represent the projectile
  val shape: Circle = new Circle {
    centerX = position.x
    centerY = position.y
    radius.value = Projectile.this.radius
    fill = shooter.color.value
  }

  // Method to update the projectile's position
  def updatePosition(): Unit = {
    position = position.add(direction.multiply(speed)).delegate
    shape.centerX.value = position.x
    shape.centerY.value = position.y
  }

  // Method to detect collision with a ship
  def hasCollidedWith(ship: Ship): Boolean = {
    shape.getBoundsInParent.intersects(ship.shape.getBoundsInParent)
  }
}
