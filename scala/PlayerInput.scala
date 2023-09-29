import scalafx.scene.input.KeyCode
import scalafx.scene.input.KeyCode.{A, D, W, Space, Left, Right, Up, Enter}

class PlayerInput(val name: String) {
  private var leftPressed = false
  private var rightPressed = false
  private var upPressed = false
  private var shootPressed = false

  def handleKeyPress(key: KeyCode): Unit = key match {
    case A if name == "Player 1" => leftPressed = true
    case D if name == "Player 1" => rightPressed = true
    case W if name == "Player 1" => upPressed = true
    case Space if name == "Player 1" => shootPressed = true
    case Left if name == "Player 2" => leftPressed = true
    case Right if name == "Player 2" => rightPressed = true
    case Up if name == "Player 2" => upPressed = true
    case Enter if name == "Player 2" => shootPressed = true
    case _ =>
  }

  def handleKeyRelease(key: KeyCode): Unit = key match {
    case A if name == "Player 1" => leftPressed = false
    case D if name == "Player 1" => rightPressed = false
    case W if name == "Player 1" => upPressed = false
    case Space if name == "Player 1" => shootPressed = false
    case Left if name == "Player 2" => leftPressed = false
    case Right if name == "Player 2" => rightPressed = false
    case Up if name == "Player 2" => upPressed = false
    case Enter if name == "Player 2" => shootPressed = false
    case _ =>
  }

  def isMovingLeft: Boolean = leftPressed
  def isMovingRight: Boolean = rightPressed
  def isMovingUp: Boolean = upPressed
  def isShooting: Boolean = shootPressed
}
