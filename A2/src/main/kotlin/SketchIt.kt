import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Cursor
import javafx.scene.ImageCursor
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class SketchIt : Application() {
    private val prefMinWidth:Double = 640.0
    private val prefMinHeight:Double = 480.0
    private val prefMaxWidth:Double = 1920.0
    private val prefMaxHeight:Double = 1440.0

    override fun start(stage: Stage) {
        // Name the stage by using the current class name
        stage.title = this.javaClass.name

        // Create and initialize the Model
        val model = Model()

        // Initialize the current canvas and toolbar with the min and max size
        val canvas = CanvasView(model)
        val toolbar = ToolbarView(model)
        val menuView = MenuView(model)

        // Use the border pane as the main layout
        canvas.prefWidth = 700.0
        canvas.prefHeight = 700.0
        val scrollPane = ScrollPane(canvas)
        val hBox = HBox()
        hBox.children.addAll(toolbar, scrollPane)
        hBox.isFillHeight = true
        val vBox = VBox()
        vBox.children.addAll(menuView, hBox)

        // Create and show scene
        val scene = Scene(vBox, prefMinWidth, prefMinHeight)

        scene.setOnKeyPressed { event ->
            if (event.code == KeyCode.ESCAPE && model.currentButton == "select") {
                model.clearBorder()
                model.modifyButton(false)
            } else if (event.code == KeyCode.DELETE && model.currentButton == "select") {
                model.deleteCurrentShape()
            }
        }
        stage.scene = scene
        stage.minWidth = prefMinWidth
        stage.minHeight = prefMinHeight
        stage.maxWidth = prefMaxWidth
        stage.maxHeight = prefMaxHeight
        stage.show()
    }
}