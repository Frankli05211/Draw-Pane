import javafx.scene.shape.Shape

interface IView {
    fun updateView(currShape:Shape?)

    fun updateShape()

    fun drawGraph()

    fun changeButtonState(isEnable:Boolean)

    fun clearBorder()

    fun clearCanvas()

    fun deleteShape()

    fun retrieveShape()

    fun pasteShapeToCanvas(newShape:Shape?)
}