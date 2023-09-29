import Main.{customHeight, customWidth}
import scalafx.animation.AnimationTimer
import scalafx.geometry.Point2D

import scala.util.Random

class GameLogic(var gameScreen: GameScreen,
                player1Input: PlayerInput,
                player1Ship: Ship,
                player2Input: PlayerInput,
                player2Ship: Ship) {

  private var powerUpActive: Boolean = false
  private var powerUpEnd: Long = 0

  // Initialize ships at left and right sides of the game screen
  player1Ship.position = new Point2D(120, customHeight / 2)
  player2Ship.position = new Point2D(customWidth - 110, customHeight / 2)


  println(s"Player 1 Ship Initial Position: ${player1Ship.position}")
  println(s"Player 2 Ship Initial Position: ${player2Ship.position}")


  private var lastPowerUpSpawnTime: Long = 0
  private val powerUpSpawnInterval: Long = 10 * 1000000000L // 10 seconds in nanoseconds

  private val powerUpSpawner: AnimationTimer = AnimationTimer { currentNanoTime: Long =>
    if (currentNanoTime - lastPowerUpSpawnTime >= powerUpSpawnInterval) {
      spawnPowerUp()
      lastPowerUpSpawnTime = currentNanoTime
    }
  }

  private def spawnPowerUp(): Unit = {
    // Randomly choose a type of power-up
    val powerUp = new Random().nextInt(4) match {
      case 0 => new GainHealthPoint()
      case 1 => new IncreasedProjectileSize()
      case 2 => new TemporaryInvulnerability()
      case 3 => new IncreasedMovementSpeed()
    }

    // Adjust the random positioning to consider screen boundaries and power-up dimensions
    val maxX = gameScreen.width.value - powerUp.shape.getBoundsInLocal.getWidth
    val maxY = gameScreen.height.value - powerUp.shape.getBoundsInLocal.getHeight

    powerUp.shape.setLayoutX(new Random().nextDouble() * maxX)
    powerUp.shape.setLayoutY(new Random().nextDouble() * maxY)

    // Add the power-up to the game screen
    gameScreen.addPowerUp(powerUp)
  }

  // Game loop
  private val gameLoop: AnimationTimer = AnimationTimer { currentNanoTime: Long =>

    gameScreen.updateProjectiles()

    // Update player ship positions based on input
    updatePlayerMovementAndActions(player1Input, player1Ship)
    updatePlayerMovementAndActions(player2Input, player2Ship)

    // Handle collision between projectiles and ships
    handleShipProjectileCollision(player1Ship, player2Ship)
    handleShipProjectileCollision(player2Ship, player1Ship)

    // Handle collision between ships and power-ups
    if (powerUpActive && currentNanoTime > powerUpEnd) {
      player1Ship.resetPowerUps() // Ensure this method resets all active power-ups
      player2Ship.resetPowerUps()
      powerUpActive = false
    }
    handleShipPowerUpCollision(player1Ship)
    handleShipPowerUpCollision(player2Ship)

    // Check game over condition
    if (player1Ship.isDestroyed) {
      gameScreen.showGameOver(player2Ship, player1Ship)
      stopGame() // stop both the game loop and the power-up spawner
    } else if (player2Ship.isDestroyed) {
      gameScreen.showGameOver(player1Ship, player2Ship)
      stopGame() // stop both the game loop and the power-up spawner
    }

    // Redraw game screen
    gameScreen.update(player1Ship :: player2Ship :: Nil)
  }

  private def updatePlayerMovementAndActions(playerInput: PlayerInput, ship: Ship): Unit = {
    ship.updateVelocity()
    ship.updatePosition()
    if (playerInput.isShooting && System.nanoTime() > ship.nextShotNanoTime) {
      ship.shoot() match {
        case Some(projectile) =>
          gameScreen.addProjectile(projectile)
        case None => // Do nothing if the ship didn't shoot a projectile
      }
    }
  }


  private def handleShipProjectileCollision(ship: Ship, otherShip: Ship): Unit = {
    val projectiles = gameScreen.getProjectiles
    for (projectile <- projectiles) {
      if (!ship.invulnerable &&
        projectile.hasCollidedWith(ship) &&
        projectile.shooter != ship) {
        ship.takeDamage()
        gameScreen.removeProjectile(projectile)

        // After the ship takes damage, update the health display
        gameScreen.updateHealthPoints(player1Ship.healthPoints, player2Ship.healthPoints)
      }
    }
  }

  private def handleShipPowerUpCollision(ship: Ship): Unit = {
    val powerUps = gameScreen.getPowerUps
    for (powerUp <- powerUps) {
      if (ship.shape.getBoundsInParent.intersects(powerUp.shape.getBoundsInParent)) {
        println(s"${ship.name} collided with ${powerUp.getClass.getSimpleName}")
        powerUp.start(ship)
        gameScreen.removePowerUp(powerUp)
        powerUpActive = true
        powerUpEnd = System.nanoTime() + powerUp.duration

        // If the power-up is a GainHealthPoint, update the health display
        if (powerUp.isInstanceOf[GainHealthPoint]) {
          gameScreen.updateHealthPoints(player1Ship.healthPoints, player2Ship.healthPoints)
        }
      }
    }
  }

  def startGame(): Unit = {
    gameLoop.start()
    powerUpSpawner.start()
  }

  def stopGame(): Unit = {
    gameLoop.stop()
    powerUpSpawner.stop()
  }

  def resetGame(): Unit = {
    // Stop the game loop and power-up spawner
    stopGame()

    // Hide game over text
    gameScreen.hideGameOver()

    // Reset ships to their initial states and positions
    player1Ship.resetShip(new Point2D(120, customHeight / 2))
    player2Ship.resetShip(new Point2D(customWidth - 110, customHeight / 2))

    // Clear existing projectiles and power-ups
    gameScreen.getProjectiles.foreach(gameScreen.removeProjectile)
    gameScreen.getPowerUps.foreach(gameScreen.removePowerUp)

    // Reset power-up state
    powerUpActive = false

    // Start the game loop and power-up spawner
    startGame()
  }

  def assignGameScreen(screen: GameScreen): Unit = {
    gameScreen = screen
    gameScreen.addShip(player1Ship)
    gameScreen.addShip(player2Ship)
  }
}
