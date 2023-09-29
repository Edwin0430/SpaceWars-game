import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, VBox}
import scalafx.geometry.Pos
import scalafx.scene.image.Image

object Main extends JFXApp {
  // Define custom screen width and height
  val customWidth = 1920
  val customHeight = 900

  // Create player inputs
  val player1Input = new PlayerInput("Player 1")
  val player2Input = new PlayerInput("Player 2")

  // Create player ships
  val player1Ship = new Ship("Player 1", Color.White, player1Input, customWidth, customHeight, isFacingRight = false)
  val player2Ship = new Ship("Player 2", Color.White, player2Input, customWidth, customHeight, isFacingRight = true)

  // Create game logic first without the gameScreen reference
  val gameLogic = new GameLogic(null, player1Input, player1Ship, player2Input, player2Ship)

  // Create game screen with custom screen bounds
  val gameScreen = new GameScreen(customWidth, customHeight, gameLogic)

  // Assign the gameScreen to the gameLogic
  gameLogic.assignGameScreen(gameScreen)

  // Load the background image
  val backgroundImage = new Image("/images/gameBackground.png")
  val backgroundImg = new BackgroundImage(backgroundImage, BackgroundRepeat.NoRepeat, BackgroundRepeat.NoRepeat, BackgroundPosition.Center, new BackgroundSize(1.0, 1.0, true, true, false, false))
  val sceneBackground = new Background(Array(backgroundImg))

  val gameScene = new Scene(customWidth, customHeight) {
    content = gameScreen
    fill = Color.Black
    onKeyPressed = k => {
      player1Input.handleKeyPress(k.code)
      player2Input.handleKeyPress(k.code)
    }
    onKeyReleased = k => {
      player1Input.handleKeyRelease(k.code)
      player2Input.handleKeyRelease(k.code)
    }
  }

  val mainMenu: Scene = new Scene(customWidth, customHeight) {
    fill = Color.Black
    root = new VBox(20) {
      alignment = Pos.Center
      children = Seq(
        new Label("Space Wars") {
          textFill = Color.White
          style = "-fx-font-size: 60pt;"
        },
        new Button("Start Game") {
          prefWidth = 300
          prefHeight = 60
          style = "-fx-font-size: 24pt;"
          onAction = _ => {
            // Show name input fields first
            val p1NameInput = new TextField {
              promptText = "Player 1 Name (Max 10 letters)"
            }
            val p2NameInput = new TextField {
              promptText = "Player 2 Name (Max 10 letters)"
            }
            val errorMsgLabel = new Label("Both player names must not exceed 10 letters!") {
              textFill = Color.Red
              style = "-fx-font-size: 18pt;"
              visible = false // Initially hidden
            }
            val nameInputScene = new Scene(customWidth, customHeight) {
              fill = Color.Black
              content = new VBox(20) {
                alignment = Pos.Center
                layoutX = customWidth * 0.5 - 250
                layoutY = customHeight * 0.5 - (3 * 60 + 2 * 20) * 0.5
                children = Seq(
                  new Label("Enter Player Names") {
                    textFill = Color.White
                    style = "-fx-font-size: 50pt;"
                  },
                  p1NameInput,
                  p2NameInput,
                  errorMsgLabel,
                  new Button("Start") {
                    prefWidth = 300
                    prefHeight = 60
                    style = "-fx-font-size: 24pt;"
                    onAction = _ => {
                      if (p1NameInput.text.value.length <= 10 && p2NameInput.text.value.length <= 10) {
                        // Update player names based on input
                        player1Ship.name = p1NameInput.text.value
                        player2Ship.name = p2NameInput.text.value

                        // Update the displayed player names on the game screen
                        gameScreen.updatePlayerNames(player1Ship.name, player2Ship.name)

                        stage.scene = gameScene
                        gameLogic.startGame()
                      } else {
                        errorMsgLabel.visible = true
                      }
                    }
                  },
                  new Button("Back") {
                    prefWidth = 300
                    prefHeight = 60
                    style = "-fx-font-size: 24pt;"
                    onAction = _ => stage.scene = mainMenu
                  }
                )
              }
            }
            stage.scene = nameInputScene
          }
        },
        new Button("How to Play") {
          prefWidth = 300
          prefHeight = 60
          style = "-fx-font-size: 24pt;"
          onAction = _ => {
            stage.scene = new Scene(customWidth, customHeight) {
              fill = Color.Black
              content = new VBox(20) {
                alignment = Pos.Center
                layoutX = customWidth * 0.5 - 250
                layoutY = customHeight * 0.5 - (3 * 60 + 2 * 20) * 0.5
                children = Seq(
                  new Label("Use arrow keys to move, space to shoot.") {
                    textFill = Color.White
                    style = "-fx-font-size: 18pt;"
                  },
                  new Button("Back") {
                    prefWidth = 300
                    prefHeight = 60
                    style = "-fx-font-size: 24pt;"
                    onAction = _ => stage.scene = mainMenu
                  }
                )
              }
            }
          }
        },
        new Button("Exit") {
          prefWidth = 300
          prefHeight = 60
          style = "-fx-font-size: 24pt;"
          onAction = _ => {
            System.exit(0)
          }
        }
      )
      background = sceneBackground
    }
  }

  // Set up primary stage
  stage = new PrimaryStage {
    title.value = "Space Wars"
    scene = mainMenu
  }
}
