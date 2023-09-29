import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Polygon}

class Ship(var name: String, initialColor: Color = Color.White, val playerInput: PlayerInput, val screenBoundsWidth: Double, val screenBoundsHeight: Double, val isFacingRight: Boolean = false) {
  // Ship attributes
  var healthPoints: Int = 3
  private var _position: Point2D = new Point2D(0, 0)
  var velocity: Point2D = new Point2D(0, 0)
  var invulnerable: Boolean = false
  var increasedProjectileSize: Boolean = false
  var increasedMovementSpeed: Boolean = false
  var nextShotNanoTime: Long = System.nanoTime()
  val color: ObjectProperty[Color] = ObjectProperty(initialColor)
  // The rotation of the ship in degrees
  var rotation: Double = 0
  // The shape of the ships
  val shape: Polygon = new Polygon {
    points.addAll(
      0.0, 0.0, // Tip of the ship (at the origin)
      if (isFacingRight) 70.0 else -70.0, 40.0, // Bottom left
      if (isFacingRight) 40.0 else -40.0, 0.0, // Center rear
      if (isFacingRight) 70.0 else -70.0, -40.0 // Bottom right
    )
    fill = color.value
  }

  def speed = if (increasedMovementSpeed) 0.15 else 0.1
  def position = _position
  def position_=(value: Point2D): Unit = {
    _position = value
    shape.translateX.value = _position.x
    shape.translateY.value = _position.y
  }

  color.onChange { (_, _, newColor) =>
    println(s"Changing ship color to: $newColor")
    shape.fill = newColor
  }

  def resetColor(): Unit = {
    color.value = Color.White
  }

  // For IncreasedProjectileSize PowerUp
  def increaseProjectileSize(isActive: Boolean): Unit = {
    increasedProjectileSize = isActive
  }

  def resetProjectileSize(): Unit = {
    increasedProjectileSize = false
  }

  // For GainHealthPoint PowerUp
  def gainHealthPoint(isActive: Boolean): Unit = {
    if (isActive) healthPoints += 1
  }

  // For TemporaryInvulnerability PowerUp
  def setInvulnerable(status: Boolean): Unit = {
    invulnerable = status
  }

  // For IncreasedMovementSpeed PowerUp
  def increaseMovementSpeed(isActive: Boolean): Unit = {
    increasedMovementSpeed = isActive
  }

  def resetMovementSpeed(): Unit = {
    increasedMovementSpeed = false
  }

  def resetPowerUps(): Unit = {
    resetProjectileSize()
    resetMovementSpeed()
    setInvulnerable(false)
  }

  def resetShip(initialPosition: Point2D): Unit = {
    healthPoints = 3 // Resetting health to initial value
    velocity = new Point2D(0, 0) // Resetting velocity to no movement
    position = initialPosition // Resetting position to the given initial position
    invulnerable = false // Disabling invulnerability
    increasedProjectileSize = false // Resetting projectile size power-up
    increasedMovementSpeed = false // Resetting movement speed power-up
    color.value = initialColor // Resetting color to initial color
    rotation = 0 // Resetting rotation to 0
    activePowerUps = List() // Clearing active power-ups list
  }

  // List of active power-ups
  var activePowerUps: List[PowerUp] = List()

  // Method to activate a power-up and change ship color accordingly
  def activatePowerUp(powerUp: PowerUp): Unit = {
    println(s"Activating power-up: ${powerUp.getClass.getSimpleName} for ship: ${name}")
    activePowerUps = powerUp :: activePowerUps
    color.value = powerUpColor(powerUp) // This sets the color based on the power-up
    println(s"Ship ${name} color set to: ${color.value}") // Log the color change
  }

  // Method to deactivate a power-up and update ship color based on remaining active power-ups
  def deactivatePowerUp(powerUp: PowerUp): Unit = {
    activePowerUps = activePowerUps.filterNot(_ == powerUp)
    powerUp.removeEffect(this)
    if (activePowerUps.nonEmpty) {
      color.value = powerUpColor(activePowerUps.head)
    } else {
      resetColor()
    }
  }

  // Helper function to determine the color for a given power-up
  def powerUpColor(powerUp: PowerUp): Color = powerUp match {
    case _: IncreasedProjectileSize => Color.Red
    case _: GainHealthPoint => Color.YellowGreen
    case _: TemporaryInvulnerability => Color.Grey
    case _: IncreasedMovementSpeed => Color.Yellow
    case _ => Color.White
  }

  // Method to update ship position based on velocity
  def updatePosition(): Unit = {
    position = position.add(velocity)
    shape.translateX.value = position.x
    shape.translateY.value = position.y
    shape.rotate.value = rotation
  }


  // Method to update ship velocity based on player input
  def updateVelocity(): Unit = {
    // Apply a damping factor to slow down the ship
    val damping = 0.95
    velocity = velocity.multiply(damping)

    // Control rotation
    if (playerInput.isMovingLeft) rotation -= 2 // Rotate left
    if (playerInput.isMovingRight) rotation += 2 // Rotate right

    // Move forward in the direction the ship is facing
    if (playerInput.isMovingUp) {
      val directionAngle = if (isFacingRight) rotation - 180 else rotation
      val direction = new Point2D(Math.cos(Math.toRadians(directionAngle)), Math.sin(Math.toRadians(directionAngle)))
      velocity = velocity.add(direction.multiply(speed))
    }

    // Keep ship's velocity within the screen bounds
    if (position.x < 0) velocity = new Point2D(Math.abs(velocity.x), velocity.y)
    if (position.x > screenBoundsWidth) velocity = new Point2D(-Math.abs(velocity.x), velocity.y)
    if (position.y < 0) velocity = new Point2D(velocity.x, Math.abs(velocity.y))
    if (position.y > screenBoundsHeight) velocity = new Point2D(velocity.x, -Math.abs(velocity.y))
  }

  // Method to check if the player is shooting
  def isShooting: Boolean = {
    playerInput.isShooting
  }

  // Method to shoot a projectile
  def shoot(): Option[Projectile] = {
    if (System.nanoTime() > nextShotNanoTime) {
      // Calculate the direction based on the ship's rotation
      val angle = rotation
      val cosAngle = Math.cos(Math.toRadians(angle))
      val sinAngle = Math.sin(Math.toRadians(angle))
      val direction = if (isFacingRight) new Point2D(-cosAngle, -sinAngle) else new Point2D(cosAngle, sinAngle)

      // Calculate the starting position at the tip of the ship
      val offset = shape.getBoundsInParent.getHeight / 2
      val startPos = position.add(direction.multiply(offset))

      // Create the projectile
      val radius = if (increasedProjectileSize) 60.0 else 20.0
      nextShotNanoTime = System.nanoTime() + 300000000
      Some(new Projectile(startPos, direction, this, radius))
    } else {
      None
    }
  }


  def takeDamage(): Unit = {
    if (!invulnerable) {
      healthPoints -= 1
      if (healthPoints <= 0) {
        // handle ship destruction logic
      }
    }
  }

  def isDestroyed: Boolean = healthPoints <= 0
}
