import javafx.scene.paint.Color
import javafx.scene.shape.Shape

class Model {
    // Store all views into this model
    private val views: ArrayList<IView> = ArrayList()

    // simple accessor method to fetch the current state
    // the data in the model, just a counter
    var currentButton:String = ""
    var currentLineColor:Color = Color.BLACK
    var currentFillColor:Color = Color.BLACK
    var currentLineWidth:Double = 0.0
    var currentLineStyle:MutableList<Double>? = null

    // Generate the array to store the current shape list
    var currShapeList:MutableList<Shape?> = mutableListOf<Shape?>()
    var currSelected:Shape? = null

    // Add views to the current model
    fun addView(view: IView) {
        views.add(view)
        view.updateView(null)
    }

    // the model uses this method to notify all of the Views that the data has changed
    // the expectation is that the Views will refresh themselves to display new data when appropriate
    private fun notifyObservers(currShape:Shape?) {
        for (view in views) {
            view.updateView(currShape)
        }
    }

    fun toolSelected(currShape: Shape?) {
        for (view in views) {
            view.updateShape()
        }
        notifyObservers(currShape)
    }

    fun loadGraph(currShape:Shape?) {
        notifyObservers(currShape)
    }

    fun modifyButton(isEnable:Boolean) {
        for (view in views) {
            view.changeButtonState(isEnable)
        }
    }

    fun retrieveShapeList() {
        notifyObservers(null)
    }

    fun clearBorder() {
        for (view in views) {
            view.clearBorder()
        }
    }

    fun clearCanvas() {
        // Initialize the current property of model
        currentButton = ""
        currentLineColor = Color.BLACK
        currentFillColor = Color.BLACK
        currentLineWidth = 0.0
        currentLineStyle = null

        for (view in views) {
            view.clearCanvas()
        }
    }

    fun updateDrawing() {
        // Initialize the current property of model
        currentButton = ""
        currentLineColor = Color.BLACK
        currentFillColor = Color.BLACK
        currentLineWidth = 0.0
        currentLineStyle = null

        // Draw the loaded drawing
        for (view in views) {
            if (currShapeList.isNotEmpty()) {
                view.drawGraph()
            }
        }
    }

    fun deleteCurrentShape() {
        for (view in views) {
            view.deleteShape()
        }
    }

    fun retrieveCurrentShape() {
        for (view in views) {
            view.retrieveShape()
        }
    }

    fun pasteShape(newShape:Shape?) {
        if (newShape != null) {
            for (view in views) {
                view.pasteShapeToCanvas(newShape)
            }
        }
    }
}