import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.ImageCursor
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Shape

internal class ToolbarView(private val model: Model) : VBox(), IView {

    // set up the toolbar as a GridPane
    private val topGridPane = GridPane()
    private val bottomGridPane = GridPane()

    private val toolToggles = ToggleGroup()
    private val lineWidthToggles = ToggleGroup()
    private val lineStyleToggles  = ToggleGroup()

    private val colorPicker1 = ColorPicker()
    private val colorPicker2 = ColorPicker()

    private val line1Button = StandardButton("Standard",35.0, 35.0)
    private val line2Button = StandardButton("Standard",35.0, 35.0)
    private val line3Button = StandardButton("Standard",35.0, 35.0)
    private val line4Button = StandardButton("Standard",35.0, 35.0)
    private val line5Button = StandardButton("Standard",35.0, 35.0)
    private val line6Button = StandardButton("Standard",35.0, 35.0)
    private val line7Button = StandardButton("Standard",35.0, 35.0)
    private val line8Button = StandardButton("Standard",35.0, 35.0)

    override fun updateView(currShape:Shape?) {
        // If no button is toggled, then we update the features in model
        //   to the original value
        if (toolToggles.selectedToggle == null) {
            model.currentButton = ""
        }

        if (lineWidthToggles.selectedToggle == null) {
            model.currentLineWidth = 0.0
        }

        if (lineStyleToggles.selectedToggle == null) {
            model.currentLineStyle = null
        }

        colorPicker1.value = model.currentLineColor
        colorPicker2.value = model.currentFillColor
        if(currShape != null) {
            val lineWidth = currShape.strokeWidth
            val lineStyle:MutableList<Double> = currShape.strokeDashArray

            when (lineWidth) {
                1.0 -> { lineWidthToggles.selectToggle(line1Button) }
                3.0 -> { lineWidthToggles.selectToggle(line2Button) }
                4.0 -> { lineWidthToggles.selectToggle(line3Button) }
                5.0 -> { lineWidthToggles.selectToggle(line4Button) }
            }

            if (lineStyle.isEmpty()) {
                lineStyleToggles.selectToggle(line5Button)
            } else if (lineStyle.contains(40.0)) {
                lineStyleToggles.selectToggle(line6Button)
            } else if (lineStyle.contains(25.0)) {
                lineStyleToggles.selectToggle(line7Button)
            } else {
                lineStyleToggles.selectToggle(line8Button)
            }

            model.currentLineWidth = lineWidth
            model.currentLineStyle = lineStyle
        }
    }

    override fun drawGraph() {
        toolToggles.selectToggle(null)
        lineWidthToggles.selectToggle(null)
        lineStyleToggles.selectToggle(null)
    }

    override fun changeButtonState(isEnable:Boolean) {
        if (isEnable) {
            line1Button.isDisable = false
            line2Button.isDisable = false
            line3Button.isDisable = false
            line4Button.isDisable = false
            line5Button.isDisable = false
            line6Button.isDisable = false
            line7Button.isDisable = false
            line8Button.isDisable = false
        } else {
            line1Button.isDisable = true
            line2Button.isDisable = true
            line3Button.isDisable = true
            line4Button.isDisable = true
            line5Button.isDisable = true
            line6Button.isDisable = true
            line7Button.isDisable = true
            line8Button.isDisable = true
        }
    }

    override fun updateShape() {}

    override fun clearCanvas() {
        toolToggles.selectToggle(null)
        lineWidthToggles.selectToggle(null)
        lineStyleToggles.selectToggle(null)
    }

    override fun clearBorder() {}

    override fun deleteShape() {}

    override fun retrieveShape() {}

    override fun pasteShapeToCanvas(newShape: Shape?) {}

    init {
        // Create a toggle group that contains all toggle buttons

        val selectionButton: ToggleButton = StandardButton("selectionTool.png", 30.0, 50.0)
        val eraseButton: ToggleButton = StandardButton("eraseTool.png", 30.0, 50.0)
        val lineButton: ToggleButton = StandardButton("lineTool.png", 30.0, 50.0)
        val circleButton: ToggleButton = StandardButton("circleTool.png", 30.0, 50.0)
        val rectangleButton: ToggleButton = StandardButton("rectangleTool.png", 30.0, 50.0)
        val fillButton: ToggleButton = StandardButton("fillTool.png", 30.0, 50.0)
        toolToggles.toggles.addAll(selectionButton, eraseButton, lineButton,
            circleButton, rectangleButton, fillButton)

        // Create mouse clicked action for each button
        selectionButton.setOnAction {
            model.currentButton = "select"
            changeButtonState(false)
        }

        eraseButton.setOnAction {
            model.currentButton = "erase"
            changeButtonState(false)
        }

        lineButton.setOnAction {
            model.currentButton = "line"
            changeButtonState(true)
        }

        circleButton.setOnAction {
            model.currentButton = "circle"
            changeButtonState(true)
        }

        rectangleButton.setOnAction {
            model.currentButton = "rectangle"
            changeButtonState(true)
        }

        fillButton.setOnAction {
            model.currentButton = "fill"
            changeButtonState(false)
        }

        // Add toggle buttons into grid pane
        topGridPane.add(selectionButton, 0, 0)
        topGridPane.add(eraseButton, 1, 0)
        topGridPane.add(lineButton, 0, 3)
        topGridPane.add(circleButton, 1, 3)
        topGridPane.add(rectangleButton, 0, 6)
        topGridPane.add(fillButton, 1, 6)
        topGridPane.alignment = Pos.CENTER_LEFT
        topGridPane.padding = Insets(5.0)
        topGridPane.hgap = 10.0
        topGridPane.vgap = 10.0

        // Create Color chooser dialog contains two color icons in our application
        colorPicker1.styleClass.add("button")
        colorPicker1.maxWidth = 70.0
        colorPicker1.value = Color.BLACK
        model.currentLineColor = colorPicker1.value
        colorPicker1.setOnAction {
            model.currentLineColor = colorPicker1.value
            model.toolSelected(null)
        }

        colorPicker2.styleClass.add("button")
        colorPicker2.maxWidth = 70.0
        colorPicker2.value = Color.BLACK
        model.currentFillColor = colorPicker2.value
        colorPicker2.setOnAction {
            model.currentFillColor = colorPicker2.value
            model.toolSelected(null)
        }
        topGridPane.add(colorPicker1, 0, 12)
        topGridPane.add(colorPicker2, 1, 12)

        // Create chooser dialog that is for selecting a line width
        val line1 = Line(0.0, 20.0, 20.0, 0.0)
        line1.strokeWidth = 2.0
        line1Button.graphic = line1
        line1Button.setOnMouseClicked {
            model.currentLineWidth = 2.0
            model.toolSelected(null)
        }

        val line2 = Line(0.0, 20.0, 20.0, 0.0)
        line2.strokeWidth = 3.0
        line2Button.graphic = line2
        line2Button.setOnMouseClicked {
            model.currentLineWidth = 3.0
            model.toolSelected(null)
        }

        val line3 = Line(0.0, 20.0, 20.0, 0.0)
        line3.strokeWidth = 4.0
        line3Button.graphic = line3
        line3Button.setOnMouseClicked {
            model.currentLineWidth = 4.0
            model.toolSelected(null)
        }

        val line4 = Line(0.0, 20.0, 20.0, 0.0)
        line4.strokeWidth = 5.0
        line4Button.graphic = line4
        line4Button.setOnMouseClicked {
            model.currentLineWidth = 5.0
            model.toolSelected(null)
        }
        lineWidthToggles.toggles.addAll(line1Button, line2Button, line3Button, line4Button)

        // Create chooser dialog that is for selecting line style
        val line5 = Line(0.0, 20.0, 20.0, 0.0)
        line5Button.graphic = line5
        line5Button.setOnMouseClicked {
            if (model.currentLineStyle == null) {
                model.currentLineStyle = mutableListOf<Double>()
            } else {
                model.currentLineStyle?.clear()
            }
            model.toolSelected(null)
        }

        val line6 = Line(0.0, 20.0, 20.0, 0.0)
        line6.strokeDashArray.addAll(9.0, 9.0)
        line6Button.graphic = line6
        line6Button.setOnMouseClicked {
            val targetLineStyle = mutableListOf<Double>(40.0, 40.0)
            if (model.currentLineStyle == null) {
                model.currentLineStyle = targetLineStyle
            } else {
                model.currentLineStyle = targetLineStyle
            }
            model.toolSelected(null)
        }

        val line7 = Line(0.0, 20.0, 20.0, 0.0)
        line7.strokeDashArray.addAll(5.0, 5.0)
        line7Button.graphic = line7
        line7Button.setOnMouseClicked {
            val targetLineStyle = mutableListOf<Double>(25.0, 25.0)
            if (model.currentLineStyle == null) {
                model.currentLineStyle = targetLineStyle
            } else {
                model.currentLineStyle = targetLineStyle
            }
            model.toolSelected(null)
        }

        val line8 = Line(0.0, 20.0, 20.0, 0.0)
        line8.strokeDashArray.addAll(3.0, 3.0)
        line8Button.graphic = line8
        line8Button.setOnMouseClicked {
            val targetLineStyle = mutableListOf<Double>(10.0, 10.0)
            if (model.currentLineStyle == null) {
                model.currentLineStyle = targetLineStyle
            } else {
                model.currentLineStyle = targetLineStyle
            }
            model.toolSelected(null)
        }
        lineStyleToggles.toggles.addAll(line5Button, line6Button, line7Button, line8Button)

        bottomGridPane.add(line1Button, 0, 1)
        bottomGridPane.add(line2Button, 1, 1)
        bottomGridPane.add(line3Button, 2, 1)
        bottomGridPane.add(line4Button, 3, 1)
        bottomGridPane.add(line5Button, 0, 3)
        bottomGridPane.add(line6Button, 1, 3)
        bottomGridPane.add(line7Button, 2, 3)
        bottomGridPane.add(line8Button, 3, 3)
        bottomGridPane.alignment = Pos.CENTER_LEFT
        bottomGridPane.padding = Insets(5.0)
        bottomGridPane.hgap = 5.0
        bottomGridPane.vgap = 5.0

        children.addAll(topGridPane, bottomGridPane)

        // register with the model when we're ready to start receiving data
        model.addView(this)
    }

    // Customized button
    // Used to set MIN, MAX, and PREFERRED sizes for all buttons
    private inner class StandardButton constructor(caption: String? = "Untitled", minLen: Double = 0.0,
        maxLen: Double = 0.0) :
        ToggleButton() {
        init {
            isVisible = true
            if (caption == "Standard") {
                setMinSize(minLen, minLen)
                setMaxSize(maxLen, maxLen)
            } else {
                val imageView = ImageView(Image(caption))
                imageView.fitWidthProperty().bind(widthProperty())
                imageView.fitHeightProperty().bind(heightProperty())
                graphic = imageView
                setMinSize(minLen, minLen)
                setMaxSize(maxLen, maxLen)
            }
        }
    }
}