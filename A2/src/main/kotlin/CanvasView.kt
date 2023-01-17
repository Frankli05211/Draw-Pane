import javafx.scene.control.Alert
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import java.util.Collections
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

internal class CanvasView(private val model: Model) : Pane(), IView {

    private val shapeList:MutableList<Shape?> = mutableListOf<Shape?>()

    // Store the mouse clicked position and state
    private var tempX:Double = -1.0
    private var tempY:Double = -1.0
    private enum class STATE { NONE, DRAG, CREATE }
    private var state = STATE.NONE

    // Initialize shape variables for generating shape
    private var currentShape:Shape? = null
    private var currentLine:Line = Line()
    private var currentCircle:Circle = Circle()
    private var currentRectangle:Rectangle = Rectangle()

    // Store the current selected shape
    private var currentSelected:Shape? = null

    // Create border around selected shape
    private var currentBorder:Shape? = null

    override fun updateView(currShape:Shape?) {
        model.currShapeList = shapeList

        this.children.clear()
        if (currentSelected != null) {
            val currIndex = shapeList.indexOf(currentSelected)
            Collections.swap(shapeList, currIndex, shapeList.size - 1)
        }
        for (shapes in shapeList) {
            this.children.add(shapes)
        }
    }

    override fun updateShape() {
        if (model.currentButton == "select") {
            currentSelected?.fill = model.currentFillColor
            currentSelected?.stroke = model.currentLineColor
            currentSelected?.strokeWidth = model.currentLineWidth
            if (model.currentLineStyle?.isNotEmpty() == true) {
                val dashValue = model.currentLineStyle?.get(0)
                currentSelected?.strokeDashArray?.clear()
                currentSelected?.strokeDashArray?.addAll(dashValue, dashValue)
            } else {
                currentSelected?.strokeDashArray?.clear()
            }
        }
    }

    override fun drawGraph() {
        for (shapes in model.currShapeList) {
            shapeList.add(shapes)
            currentShape = shapes
            addEventToCurrentShape()
        }
        updateView(null)
    }

    override fun changeButtonState(isEnable:Boolean) {}

    override fun clearCanvas() {
        shapeList.clear()
        currentShape = null
        currentSelected = null
        currentBorder = null
        this.children.clear()
    }

    override fun clearBorder() {
        removeBorder()
        currentSelected = null
    }

    override fun deleteShape() {
        shapeList.remove(currentSelected)
        removeBorder()
        currentSelected = null
        updateView(null)
    }

    override fun retrieveShape() {
        model.currSelected = currentSelected
    }

    override fun pasteShapeToCanvas(newShape: Shape?) {
        removeBorder()
        shapeList.add(newShape)
        currentShape = newShape
        addEventToCurrentShape()
        updateView(null)
    }

    init {
        // Record the start position of the graph that may be drawn if there is a mouse clicked
        this.setOnMousePressed {event ->
            tempX = event.x
            tempY = event.y
            removeBorder()
            when (model.currentButton) {
                "line" -> {
                    currentShape = Line(tempX, tempY, tempX, tempY)
                    state = STATE.CREATE
                }
                "circle" -> {
                    currentShape = Circle(tempX, tempY, 0.0)
                    state = STATE.CREATE
                }
                "rectangle" -> {
                    currentShape = Rectangle(tempX, tempY, 0.0, 0.0)
                    state = STATE.CREATE
                }
                "select", "erase", "fill" -> {
                    model.modifyButton(false)
                }
            }

            if (state == STATE.CREATE) {
                if (canDraw()) {
                    currentShape?.fill = model.currentFillColor
                    currentShape?.stroke = model.currentLineColor
                    currentShape?.strokeWidth = model.currentLineWidth
                    if (model.currentLineStyle?.isNotEmpty() == true) {
                        val dashValue = model.currentLineStyle?.get(0)
                        currentShape?.strokeDashArray?.addAll(dashValue, dashValue)
                    }
                    shapeList.add(currentShape)
                    updateView(currentSelected)
                } else {
                    currentShape = null
                    state = STATE.NONE
                }
            }

            currentSelected = null
            updateView(currentSelected)
        }

        this.setOnMouseDragged { event ->
            if (state == STATE.CREATE || state == STATE.DRAG) {
                val dx = event.x - tempX
                val dy = event.y - tempY
                when (currentShape?.typeSelector) {
                    "Line" -> {
                        currentLine = currentShape as Line
                        currentLine.endX = event.x
                        currentLine.endY = event.y
                        state = STATE.DRAG
                        currentSelected = currentShape
                        updateView(currentSelected)
                    }
                    "Circle" -> {
                        currentCircle = currentShape as Circle
                        val newRadius = min(dx/2, dy/2)
                        currentCircle.centerX = tempX + newRadius
                        currentCircle.centerY = tempY + newRadius
                        currentCircle.radius = newRadius
                        state = STATE.DRAG
                        currentSelected = currentShape
                        updateView(currentSelected)
                    }
                    "Rectangle" -> {
                        currentRectangle = currentShape as Rectangle
                        currentRectangle.width = dx
                        currentRectangle.height = dy
                        state = STATE.DRAG
                        currentSelected = currentShape
                        updateView(currentSelected)
                    }
                }
            }
        }

        this.setOnMouseReleased {
            if (state == STATE.CREATE) {
                // If mouse is not dragged to draw the shape, we remove the empty shape created
                shapeList.remove(currentShape)
            } else if (state == STATE.DRAG) {
                // If there is a shape created, we will add drag event handler to that shape
                addEventToCurrentShape()
            }
            state = STATE.NONE
            updateView(currentSelected)
        }

        updateView(currentSelected)
        model.addView(this)
    }

    // This function returns true if the information in model class is enough to draw
    //   the current shape, otherwise the function will fire a dialog to mention the
    //   user about the missing part
    private fun canDraw():Boolean {
        return if (model.currentLineWidth == 0.0) {
            val dialog = Alert(Alert.AlertType.ERROR)
            dialog.title = "Warning"
            dialog.contentText = "Please select a valid border width from the left toolBar."
            dialog.showAndWait()
            false
        } else if (model.currentLineStyle == null) {
            val dialog = Alert(Alert.AlertType.ERROR)
            dialog.title = "Warning"
            dialog.contentText = "Please select a valid border style from the left toolBar."
            dialog.showAndWait()
            false
        } else {
            true
        }
    }

    private fun removeBorder() {
        if (currentBorder != null) {
            this.children.remove(currentBorder)
            shapeList.remove(currentBorder)
            currentBorder = null
        }
    }

    private fun buildBorder() {
        removeBorder()

        if (currentSelected != null) {
            // Initialize value needed for generating border rectangle
            var xPos = 0.0
            var yPos = 0.0
            var recWidth = 0.0
            var recHeight = 0.0

            // Modify the above generated value according to the current shape
            when (currentSelected?.typeSelector) {
                "Line" -> {
                    currentLine = currentSelected as Line
                    xPos = if (currentLine.startX > currentLine.endX) {
                        currentLine.endX - 6.0
                    } else {
                        currentLine.startX - 6.0
                    }

                    yPos = if (currentLine.startY > currentLine.endY) {
                        currentLine.endY - 6.0
                    } else {
                        currentLine.startY - 6.0
                    }
                    recWidth = abs(currentLine.endX - currentLine.startX) + 12.0
                    recHeight = abs(currentLine.endY - currentLine.startY) + 12.0
                }
                "Circle" -> {
                    currentCircle = currentSelected as Circle
                    xPos = currentCircle.centerX - currentCircle.radius - 6.0
                    yPos = currentCircle.centerY - currentCircle.radius - 6.0
                    recWidth = currentCircle.radius * 2 + 12.0
                    recHeight = currentCircle.radius * 2 + 12.0
                }
                "Rectangle" -> {
                    currentRectangle = currentSelected as Rectangle
                    xPos = currentRectangle.x - 6.0
                    yPos = currentRectangle.y - 6.0
                    recWidth = currentRectangle.width + 12.0
                    recHeight = currentRectangle.height + 12.0
                }
            }

            currentBorder = Rectangle(xPos, yPos, recWidth, recHeight)
            currentBorder?.fill = Color.TRANSPARENT
            currentBorder?.strokeWidth = 1.0
            currentBorder?.stroke = Color.BLACK
            shapeList.add(currentBorder)
        }
        updateView(currentBorder)
    }

    private fun addEventToCurrentShape() {
        when(currentShape?.typeSelector) {
            "Line" -> {
                currentLine = currentShape as Line
                with(currentLine) {
                    this.setOnMousePressed { event ->
                        when (model.currentButton) {
                            "select" -> {
                                currentSelected = this
                                tempX = event.x
                                tempY = event.y
                                model.currentLineColor = this.stroke as Color
                                model.modifyButton(true)
                                model.loadGraph(currentSelected)
                                buildBorder()
                                event.consume()
                            }
                            "erase" -> {
                                shapeList.remove(this)
                                removeBorder()
                                currentSelected = null
                                event.consume()
                            }
                            "fill" -> {
                                this.stroke = model.currentLineColor
                            }
                        }
                    }
                    this.setOnMouseDragged { event ->
                        if (model.currentButton == "select") {
                            val dx = event.x - tempX
                            val dy = event.y - tempY
                            tempX = event.x
                            tempY = event.y
                            this.startX += dx
                            this.startY += dy
                            this.endX += dx
                            this.endY += dy
                            event.consume()
                        }
                        if (currentBorder != null) {
                            removeBorder()
                        }
                    }
                    this.setOnMouseReleased {
                        buildBorder()
                    }
                }
            }
            "Circle" -> {
                currentCircle = currentShape as Circle
                with(currentCircle) {
                    this.setOnMousePressed { event ->
                        when (model.currentButton) {
                            "select" -> {
                                currentSelected = this
                                tempX = event.x
                                tempY = event.y
                                model.currentLineColor = this.stroke as Color
                                model.currentFillColor = this.fill as Color
                                model.modifyButton(true)
                                model.loadGraph(currentSelected)
                                buildBorder()
                                event.consume()
                            }
                            "erase" -> {
                                shapeList.remove(this)
                                removeBorder()
                                currentSelected = null
                                event.consume()
                            }
                            "fill" -> {
                                this.fill = model.currentFillColor
                            }
                        }
                    }
                    this.setOnMouseDragged { event ->
                        if (model.currentButton == "select") {
                            val dx = event.x - tempX
                            val dy = event.y - tempY
                            tempX = event.x
                            tempY = event.y
                            this.centerX += dx
                            this.centerY += dy
                            event.consume()
                        }
                        if (currentBorder != null) {
                            removeBorder()
                        }
                    }
                    this.setOnMouseReleased {
                        buildBorder()
                    }
                }
            }
            "Rectangle" -> {
                currentRectangle = currentShape as Rectangle
                with(currentRectangle) {
                    this.setOnMousePressed { event ->
                        when (model.currentButton) {
                            "select" -> {
                                currentSelected = this
                                tempX = event.x
                                tempY = event.y
                                model.currentLineColor = this.stroke as Color
                                model.currentFillColor = this.fill as Color
                                model.modifyButton(true)
                                model.loadGraph(currentSelected)
                                buildBorder()
                                event.consume()
                            }
                            "erase" -> {
                                shapeList.remove(this)
                                removeBorder()
                                currentSelected = null
                                event.consume()
                            }
                            "fill" -> {
                                this.fill = model.currentFillColor
                            }
                        }
                    }
                    this.setOnMouseDragged { event ->
                        if (model.currentButton == "select") {
                            val dx = event.x - tempX
                            val dy = event.y - tempY
                            tempX = event.x
                            tempY = event.y
                            this.x += dx
                            this.y += dy
                            event.consume()
                        }
                        if (currentBorder != null) {
                            removeBorder()
                        }
                    }
                    this.setOnMouseReleased {
                        buildBorder()
                    }
                }
            }
        }
    }
}