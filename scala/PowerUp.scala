import scalafx.animation.{Timeline, KeyFrame}
import scalafx.scene.image.{Image, ImageView}
import scalafx.util.Duration
import java.util.concurrent.atomic.AtomicInteger

sealed abstract class PowerUp {
  def shape: ImageView
  val duration: Long

  def applyEffect(ship: Ship): Unit
  def removeEffect(ship: Ship): Unit

  def start(ship: Ship): Unit = {
    println(s"Starting power-up: ${this.getClass.getSimpleName} for ship: ${ship.name}")

    val currentPowerUp = this
    applyEffect(ship)
    new Timeline {
      cycleCount = 1
      keyFrames = Seq(
        KeyFrame(Duration(duration / 1000000), onFinished = _ => {
          removeEffect(ship)
          ship.deactivatePowerUp(currentPowerUp)
        })
      )
      play()
    }
  }
}

class IncreasedProjectileSize extends PowerUp {
  val shape = new ImageView(new Image("/images/increaseProjectileSize.png"))
  val duration: Long = 10 * 1000000000L  // 10 seconds in nanoseconds
  shape.scaleX = 0.2
  shape.scaleY = 0.2
  private val count = new AtomicInteger(0)

  override def applyEffect(ship: Ship): Unit = {
    if (count.getAndIncrement() == 0) {
      ship.increaseProjectileSize(true)
      ship.activatePowerUp(this)
    }
  }

  override def removeEffect(ship: Ship): Unit = {
    if (count.decrementAndGet() == 0) {
      ship.resetProjectileSize()
    }
  }
}

class GainHealthPoint extends PowerUp {
  val shape = new ImageView(new Image("/images/gainHealth.png"))
  val duration: Long = 1 * 1000000000L  // 2 seconds in nanoseconds
  shape.scaleX = 0.4
  shape.scaleY = 0.4

  override def applyEffect(ship: Ship): Unit = {
    ship.gainHealthPoint(true)
    ship.activatePowerUp(this)
  }


  override def removeEffect(ship: Ship): Unit = {
    ship.resetColor()
  }
}

class TemporaryInvulnerability extends PowerUp {
  val shape = new ImageView(new Image("/images/temporaryInvulnerability.png"))
  val duration: Long = 10 * 1000000000L  // 10 seconds in nanoseconds
  shape.scaleX = 0.19
  shape.scaleY = 0.19
  private val count = new AtomicInteger(0)

  override def applyEffect(ship: Ship): Unit = {
    if (count.getAndIncrement() == 0) {
      ship.setInvulnerable(true)
      ship.activatePowerUp(this)
    }
  }

  override def removeEffect(ship: Ship): Unit = {
    if (count.decrementAndGet() == 0) {
      ship.setInvulnerable(false)
    }
  }
}

class IncreasedMovementSpeed extends PowerUp {
  val shape = new ImageView(new Image("/images/increaseMovementSpeed.png"))
  val duration: Long = 10 * 1000000000L  // 10 seconds in nanoseconds
  shape.scaleX = 0.5
  shape.scaleY = 0.5
  private val count = new AtomicInteger(0)

  override def applyEffect(ship: Ship): Unit = {
    if (count.getAndIncrement() == 0) {
      ship.increaseMovementSpeed(true)
      ship.activatePowerUp(this)
    }
  }

  override def removeEffect(ship: Ship): Unit = {
    if (count.decrementAndGet() == 0) {
      ship.resetMovementSpeed()
    }
  }
}
