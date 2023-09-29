import scalafx.scene.Group
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color

class GameScreen(prefWidth: Double, prefHeight: Double, gameLogic: GameLogic) extends Pane {
  this.setPrefSize(prefWidth, prefHeight)

  private val shipsGroup = new Group()
  private val projectilesGroup = new Group()
  private val powerUpsGroup = new Group()
  private val powerUpsBuffer = scala.collection.mutable.Buffer[PowerUp]()

  // Load the background image for the game screen
  val backgroundImage = new Image("/images/gameBackground.png")
  val backgroundImg = new BackgroundImage(backgroundImage, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, new BackgroundSize(1.0, 1.0, true, true, false, false))
  val sceneBackground = new Background(Array(backgroundImg))
  background = sceneBackground // Set background to the game screen



  class GameScreen(prefWidth: Double, prefHeight: Double, gameLogic: GameLogic) extends Pane {
    this.setPrefSize(prefWidth, prefHeight)

    private val shipsGroup = new Group()
    private val projectilesGroup = new Group()
    private val powerUpsGroup = new Group()
    private val powerUpsBuffer = scala.collection.mutable.Buffer[PowerUp]()

    // Load the background image
    val backgroundImage = new Image("/images/gameBackground.png")
    val backgroundImg = new BackgroundImage(backgroundImage, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, new BackgroundSize(1.0, 1.0, true, true, false, false))
    val sceneBackground = new Background(Array(backgroundImg))

    // Heart image for health representation
    private val heartImage = new Image("/images/healthHeart.png")

    // Player name labels
    private val player1NameLabel = new Label {
      textFill = Color.White
      style = "-fx-font-size: 20pt;"
      alignment = Pos.Center
      background = new Background(Array(new BackgroundFill(Color.Blue, CornerRadii.Empty, Insets.Empty)))
      prefWidth = 180
      prefHeight = 60
    }

    private val player2NameLabel = new Label {
      textFill = Color.White
      style = "-fx-font-size: 20pt;"
      alignment = Pos.Center
      background = new Background(Array(new BackgroundFill(Color.OrangeRed, CornerRadii.Empty, Insets.Empty)))
      prefWidth = 180
      prefHeight = 60
    }

    // Health display for players
    private val player1Hearts: Seq[ImageView] = for (_ <- 1 to 3) yield {
      val imageView = new ImageView(heartImage)
      imageView.scaleX = 0.5
      imageView.scaleY = 0.5
      imageView
    }

    private val player2Hearts: Seq[ImageView] = for (_ <- 1 to 3) yield {
      val imageView = new ImageView(heartImage)
      imageView.scaleX = 0.5
      imageView.scaleY = 0.5
      imageView
    }

    // Collection to keep track of projectiles
    private val projectiles = scala.collection.mutable.Buffer[Projectile]()

    // Game Over Components
    val gameOverLabel = new Label {
      textFill = Color.White
      style = "-fx-font-size: 30pt;"
      alignment = Pos.Center
      layoutX = 0
      layoutY = 0
      prefWidth = 300
      prefHeight = 80
    }

    val retryButton = new Button("Retry") {
      layoutX = 0
      layoutY = 90
      prefWidth = 300
      prefHeight = 50
      style = "-fx-font-size: 20pt;"
    }

    retryButton.onAction = _ => {
      // Hide the game over components
      gameOverLabel.visible = false
      retryButton.visible = false
      exitButton.visible = false

      // Show the game components
      shipsGroup.visible = true
      projectilesGroup.visible = true
      powerUpsGroup.visible = true
      player1NameLabel.visible = true
      player2NameLabel.visible = true
      player1Hearts.foreach(_.visible = true)
      player2Hearts.foreach(_.visible = true)

      gameLogic.resetGame()
    }

    val exitButton = new Button("Exit") {
      layoutX = 0
      layoutY = 150
      prefWidth = 300
      prefHeight = 50
      style = "-fx-font-size: 20pt;"
      onAction = _ => System.exit(0)
    }

    val gameOver: VBox = new VBox(20) {
      alignment = Pos.Center
      children = Seq(
        new Label("Game Over") {
          textFill = Color.White
          style = "-fx-font-size: 60pt;"
        },
        gameOverLabel,
        retryButton,
        exitButton
      )
      visible = false
    }

    // Center the gameOver VBox
    gameOver.layoutX = (prefWidth - 300) * 0.5 // assuming 300 is the width of its widest child
    gameOver.layoutY = (prefHeight - (3 * 60 + 2 * 20)) * 0.5 // adjust as necessary based on the total height


    // Combine children assignments into one
    children = List(
      shipsGroup, projectilesGroup, powerUpsGroup,
      player1NameLabel, player2NameLabel,
      gameOver
    ) ++ player1Hearts ++ player2Hearts


    // Position player 1 hearts and labels
    player1Hearts.zipWithIndex.foreach { case (heart, index) =>
      heart.layoutX = 10 + (index * (heartImage.getWidth * 0.5 + 5))
      heart.layoutY = 20
    }

    player1NameLabel.layoutX = 130
    player1NameLabel.layoutY = 60 + heartImage.getHeight * 0.5 + 10

    // Position player 2 hearts and labels
    player2Hearts.zipWithIndex.foreach { case (heart, index) =>
      heart.layoutX = prefWidth - 140 - (index + 1) * (heartImage.getWidth * 0.5 + 5)
      heart.layoutY = 20
    }

    player2NameLabel.layoutX = prefWidth - 300 - player2NameLabel.width.value
    player2NameLabel.layoutY = 60 + heartImage.getHeight * 0.5 + 10

    // Game operations
    def addShip(ship: Ship): Unit = {
      shipsGroup.children.add(ship.shape)
    }

    def addProjectile(projectile: Projectile): Unit = {
      projectiles += projectile
      projectilesGroup.children.add(projectile.shape)
    }

    def removeProjectile(projectile: Projectile): Unit = {
      projectiles -= projectile
      projectilesGroup.children.remove(projectile.shape)
    }

    def updateProjectiles(): Unit = {
      projectiles.foreach(_.updatePosition())
      val offScreenProjectiles = projectiles.filter(p =>
        p.position.x < 0 || p.position.x > prefWidth || p.position.y < 0 || p.position.y > prefHeight
      )
      offScreenProjectiles.foreach(removeProjectile)
    }

    def addPowerUp(powerUp: PowerUp): Unit = {
      powerUpsBuffer += powerUp
      powerUpsGroup.children.add(powerUp.shape)
    }

    def removePowerUp(powerUp: PowerUp): Unit = {
      powerUpsBuffer -= powerUp
      powerUpsGroup.children.remove(powerUp.shape)
    }

    def getProjectiles: List[Projectile] = {
      projectiles.toList
    }

    def getPowerUps: List[PowerUp] = {
      powerUpsBuffer.toList
    }

    def update(playerShips: List[Ship]): Unit = {
      playerShips.foreach { ship =>
        ship.updatePosition()
        ship.setInvulnerable(ship.invulnerable)
      }
    }

    def showGameOver(winner: Ship, loser: Ship): Unit = {
      shipsGroup.visible = false
      projectilesGroup.visible = false
      powerUpsGroup.visible = false
      player1NameLabel.visible = false
      player2NameLabel.visible = false
      player1Hearts.foreach(_.visible = false)
      player2Hearts.foreach(_.visible = false)

      gameOverLabel.text = s"${winner.name} Wins!"
      gameOver.visible = true // Ensure the VBox itself is visible
      retryButton.visible = true
      exitButton.visible = true
      gameOverLabel.visible = true
    }

    def hideGameOver(): Unit = {
      gameOver.visible = false
    }


    def updateHealthPoints(player1HealthPoints: Int, player2HealthPoints: Int): Unit = {
      player1Hearts.zipWithIndex.foreach { case (heart, index) =>
        heart.visible = index < player1HealthPoints
      }

      player2Hearts.zipWithIndex.foreach { case (heart, index) =>
        heart.visible = index < player2HealthPoints
      }
    }

    def updatePlayerNames(player1Name: String, player2Name: String): Unit = {
      player1NameLabel.text = player1Name
      player2NameLabel.text = player2Name
    }
  }


  // Heart image for health representation
  private val heartImage = new Image("/images/healthHeart.png")

  // Player name labels
  private val player1NameLabel = new Label {
    textFill = Color.White
    style = "-fx-font-size: 20pt;"
    alignment = Pos.Center
    background = new Background(Array(new BackgroundFill(Color.Blue, CornerRadii.Empty, Insets.Empty)))
    prefWidth = 180
    prefHeight = 60
  }

  private val player2NameLabel = new Label {
    textFill = Color.White
    style = "-fx-font-size: 20pt;"
    alignment = Pos.Center
    background = new Background(Array(new BackgroundFill(Color.OrangeRed, CornerRadii.Empty, Insets.Empty)))
    prefWidth = 180
    prefHeight = 60
  }

  // Health display for players
  private val player1Hearts: Seq[ImageView] = for (_ <- 1 to 3) yield {
    val imageView = new ImageView(heartImage)
    imageView.scaleX = 0.5
    imageView.scaleY = 0.5
    imageView
  }

  private val player2Hearts: Seq[ImageView] = for (_ <- 1 to 3) yield {
    val imageView = new ImageView(heartImage)
    imageView.scaleX = 0.5
    imageView.scaleY = 0.5
    imageView
  }

  // Collection to keep track of projectiles
  private val projectiles = scala.collection.mutable.Buffer[Projectile]()

  // Game Over Components
  val gameOverLabel = new Label {
    textFill = Color.White
    style = "-fx-font-size: 30pt;"
    alignment = Pos.Center
    layoutX = 0
    layoutY = 0
    prefWidth = 300
    prefHeight = 80
  }

  val retryButton = new Button("Retry") {
    layoutX = 0
    layoutY = 90
    prefWidth = 300
    prefHeight = 50
    style = "-fx-font-size: 20pt;"
  }

  retryButton.onAction = _ => {
    // Hide the game over components
    gameOverLabel.visible = false
    retryButton.visible = false
    exitButton.visible = false

    // Show the game components
    shipsGroup.visible = true
    projectilesGroup.visible = true
    powerUpsGroup.visible = true
    player1NameLabel.visible = true
    player2NameLabel.visible = true
    player1Hearts.foreach(_.visible = true)
    player2Hearts.foreach(_.visible = true)

    gameLogic.resetGame()
  }

  val exitButton = new Button("Exit") {
    layoutX = 0
    layoutY = 150
    prefWidth = 300
    prefHeight = 50
    style = "-fx-font-size: 20pt;"
    onAction = _ => System.exit(0)
  }

  val gameOver: VBox = new VBox(20) {
    alignment = Pos.Center
    children = Seq(
      new Label("Game Over") {
        textFill = Color.White
        style = "-fx-font-size: 60pt;"
      },
      gameOverLabel,
      retryButton,
      exitButton
    )
    visible = false
  }

  // Center the gameOver VBox
  gameOver.layoutX = (prefWidth - 300) * 0.5 // assuming 300 is the width of its widest child
  gameOver.layoutY = (prefHeight - (3 * 60 + 2 * 20)) * 0.5 // adjust as necessary based on the total height


  // Combine children assignments into one
  children = List(
    shipsGroup, projectilesGroup, powerUpsGroup,
    player1NameLabel, player2NameLabel,
    gameOver
  ) ++ player1Hearts ++ player2Hearts


  // Position player 1 hearts and labels
  player1Hearts.zipWithIndex.foreach { case (heart, index) =>
    heart.layoutX = 10 + (index * (heartImage.getWidth * 0.5 + 5))
    heart.layoutY = 20
  }

  player1NameLabel.layoutX = 130
  player1NameLabel.layoutY = 60 + heartImage.getHeight * 0.5 + 10

  // Position player 2 hearts and labels
  player2Hearts.zipWithIndex.foreach { case (heart, index) =>
    heart.layoutX = prefWidth - 140 - (index + 1) * (heartImage.getWidth * 0.5 + 5)
    heart.layoutY = 20
  }

  player2NameLabel.layoutX = prefWidth - 300 - player2NameLabel.width.value
  player2NameLabel.layoutY = 60 + heartImage.getHeight * 0.5 + 10

  // Game operations
  def addShip(ship: Ship): Unit = {
    shipsGroup.children.add(ship.shape)
  }

  def removeShip(ship: Ship): Unit = {
    shipsGroup.children.remove(ship.shape)
  }

  def addProjectile(projectile: Projectile): Unit = {
    projectiles += projectile
    projectilesGroup.children.add(projectile.shape)
  }

  def removeProjectile(projectile: Projectile): Unit = {
    projectiles -= projectile
    projectilesGroup.children.remove(projectile.shape)
  }

  def updateProjectiles(): Unit = {
    projectiles.foreach(_.updatePosition())
    val offScreenProjectiles = projectiles.filter(p =>
      p.position.x < 0 || p.position.x > prefWidth || p.position.y < 0 || p.position.y > prefHeight
    )
    offScreenProjectiles.foreach(removeProjectile)
  }

  def addPowerUp(powerUp: PowerUp): Unit = {
    powerUpsBuffer += powerUp
    powerUpsGroup.children.add(powerUp.shape)
  }

  def removePowerUp(powerUp: PowerUp): Unit = {
    powerUpsBuffer -= powerUp
    powerUpsGroup.children.remove(powerUp.shape)
  }

  def getProjectiles: List[Projectile] = {
    projectiles.toList
  }

  def getPowerUps: List[PowerUp] = {
    powerUpsBuffer.toList
  }

  def update(playerShips: List[Ship]): Unit = {
    playerShips.foreach { ship =>
      ship.updatePosition()
      ship.setInvulnerable(ship.invulnerable)
    }
  }

  def showGameOver(winner: Ship, loser: Ship): Unit = {
    shipsGroup.visible = false
    projectilesGroup.visible = false
    powerUpsGroup.visible = false
    player1NameLabel.visible = false
    player2NameLabel.visible = false
    player1Hearts.foreach(_.visible = false)
    player2Hearts.foreach(_.visible = false)

    gameOverLabel.text = s"${winner.name} Wins!"
    gameOver.visible = true // Ensure the VBox itself is visible
    retryButton.visible = true
    exitButton.visible = true
    gameOverLabel.visible = true
  }

  def hideGameOver(): Unit = {
    gameOver.visible = false
  }


  def updateHealthPoints(player1HealthPoints: Int, player2HealthPoints: Int): Unit = {
    player1Hearts.zipWithIndex.foreach { case (heart, index) =>
      heart.visible = index < player1HealthPoints
    }

    player2Hearts.zipWithIndex.foreach { case (heart, index) =>
      heart.visible = index < player2HealthPoints
    }
  }

  def updatePlayerNames(player1Name: String, player2Name: String): Unit = {
    player1NameLabel.text = player1Name
    player2NameLabel.text = player2Name
  }
}
