import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import java.io.*
import java.util.*

internal class MenuView(private val model: Model) : MenuBar(), IView {
    private var isSaved = false
    private val rootDirectory = File("${System.getProperty("user.dir")}\\save")
    private var currFile:File? = null

    override fun updateView(currShape: Shape?) { isSaved = false }

    override fun drawGraph() {}

    override fun updateShape() {}

    override fun changeButtonState(isEnable:Boolean) {}

    override fun clearCanvas() {}

    override fun clearBorder() {}

    override fun deleteShape() {}

    override fun retrieveShape() {}

    override fun pasteShapeToCanvas(newShape: Shape?) {}

    init {
        val fileMenu = Menu("File")
        val helpMenu = Menu("Help")
        val editMenu = Menu("Edit")

        // Add menuItem to the menus above
        val menuNew = MenuItem("New")
        val menuLoad = MenuItem("Load")
        val menuSave = MenuItem("Save")
        val menuQuit = MenuItem("Quit")
        val menuAbout = MenuItem("About")
        val menuCut = MenuItem("Cut")
        val menuCopy = MenuItem("Copy")
        val menuPaste = MenuItem("Paste")

        // Add keyboard shortcut for cut, copy and paste
        menuCut.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)
        menuCopy.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
        menuPaste.accelerator = KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN)

        // Add action for each menu item
        menuNew.setOnAction {
            // Retrieve Shape list from canvas
            val shouldSave = isSaved
            model.clearBorder()
            model.retrieveShapeList()
            val canvas = model.currShapeList

            // If the current drawing is not empty, prompt the user to save if the current drawing is unsaved
            if (!shouldSave and canvas.isNotEmpty()) {
                val confirmation = Alert(Alert.AlertType.CONFIRMATION)
                confirmation.title = "Current drawing is unsaved"
                confirmation.contentText = "Do you wish to save the current drawing?"
                val result = confirmation.showAndWait()

                if (result.isPresent) {
                    when (result.get()) {
                        ButtonType.OK -> {menuSave.fire()}
                    }
                }
            }

            model.clearCanvas()
            isSaved = false
        }

        menuLoad.setOnAction {
            // Retrieve Shape list from canvas
            val shouldSave = isSaved
            model.clearBorder()
            model.retrieveShapeList()
            val canvas = model.currShapeList

            // If the current drawing is not empty, prompt the user to save if the current drawing is unsaved
            if (canvas.isNotEmpty() && !shouldSave) {
                val confirmation = Alert(Alert.AlertType.CONFIRMATION)
                confirmation.title = "Current drawing is unsaved"
                confirmation.contentText = "Do you wish to save the current drawing?"
                val result = confirmation.showAndWait()

                if (result.isPresent) {
                    when (result.get()) {
                        ButtonType.OK -> {menuSave.fire()}
                    }
                }
            }

            // After working with the current drawing, we are going to load the expected file
            val fileDialog = TextInputDialog("")
            fileDialog.title = "Load saved drawing"
            fileDialog.headerText = "Enter the File name that you want to load"

            val result = fileDialog.showAndWait()
            if (result.isPresent) {
                currFile = File(rootDirectory.absolutePath + "\\${result.get()}")
                if (!rootDirectory.listFiles().contains(currFile)) {
                    val warning = Alert(Alert.AlertType.ERROR)
                    warning.title = "Warning"
                    warning.contentText = "Cannot find ${result.get()} in ./save directory\nPlease enter an file name that exists"
                    warning.showAndWait()
                } else {
                    val loadShapeList = loadDrawing(currFile)
                    model.clearCanvas()
                    model.currShapeList = loadShapeList
                    model.updateDrawing()
                    isSaved = true
                }
            }
        }

        menuSave.setOnAction {
            val fileDialog = TextInputDialog("")
            fileDialog.title = "Save current drawing"
            fileDialog.headerText = "Enter the File name that you want to save"

            val result = fileDialog.showAndWait()
            if (result.isPresent) {
                currFile = File(rootDirectory.absolutePath + "\\${result.get()}")
                if (currFile?.extension != "txt") {
                    val warning = Alert(Alert.AlertType.ERROR)
                    warning.title = "Warning"
                    warning.contentText = "Please save your drawing into a .txt file by entering {fileName}.txt"
                    warning.showAndWait()
                } else {
                    // Retrieve Shape list from canvas
                    model.clearBorder()
                    model.retrieveShapeList()
                    val canvas = model.currShapeList

                    // Save drawing into curreFile
                    saveDrawing(currFile, canvas)
                    isSaved = true
                }
            }
        }

        menuQuit.setOnAction {
            // Retrieve Shape list from canvas
            val shouldSave = isSaved
            model.clearBorder()
            model.retrieveShapeList()
            val canvas = model.currShapeList

            // If the current drawing is not empty, prompt the user to save if the current drawing is unsaved
            if (canvas.isNotEmpty() && !shouldSave) {
                val confirmation = Alert(Alert.AlertType.CONFIRMATION)
                confirmation.title = "Current drawing is unsaved"
                confirmation.contentText = "Do you wish to save the current drawing?"
                val result = confirmation.showAndWait()

                if (result.isPresent) {
                    when (result.get()) {
                        ButtonType.OK -> {menuSave.fire()}
                    }
                }
            }
            Platform.exit()
        }

        menuAbout.setOnAction {
            // informational dialog from dialog demo in widgets of sample code
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Info"
            alert.contentText = "Application Name: Sketch it\nDeveloper: Boyang Li\nWatID:20847992"
            alert.showAndWait()
        }

        menuCut.onAction = EventHandler {
            putOnClipboard()
            model.deleteCurrentShape()
        }

        menuCopy.onAction = EventHandler {
            putOnClipboard()
        }

        menuPaste.onAction = EventHandler {
            val newShape = getFromClipboard()
            if (newShape != null) {
                model.pasteShape(newShape)
            }
        }

        // Append menuItems to menu
        fileMenu.items.addAll(menuNew, menuLoad, menuSave, menuQuit)
        helpMenu.items.addAll(menuAbout)
        editMenu.items.addAll(menuCut, menuCopy, menuPaste)

        this.menus.addAll(fileMenu, helpMenu, editMenu)

        model.addView(this)
    }

    // The function will save the current canvas into saveFile
    private fun saveDrawing(saveFile:File?, canvas:MutableList<Shape?>) {
        // Use buffered writer to write each shapes' property into the file
        val writer:BufferedWriter = BufferedWriter(FileWriter(saveFile))

        // For each shapes in the shapelist we store its property into file
        for (shapes in canvas) {
            if (shapes != null) {
                var savedString = shapes?.typeSelector
                writer.write(savedString)
                writer.newLine()
                when (shapes?.typeSelector) {
                    "Line" -> {
                        val currentLine = shapes as Line
                        savedString = (currentLine.startX).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentLine.startY).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentLine.endX).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentLine.endY).toString()
                        writer.write(savedString)
                        writer.newLine()
                    }
                    "Circle" -> {
                        val currentCircle = shapes as Circle
                        savedString = (currentCircle.centerX).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentCircle.centerY).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentCircle.radius).toString()
                        writer.write(savedString)
                        writer.newLine()
                    }
                    "Rectangle" -> {
                        val currentRec = shapes as Rectangle
                        savedString = (currentRec.x).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentRec.y).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentRec.width).toString()
                        writer.write(savedString)
                        writer.newLine()
                        savedString = (currentRec.height).toString()
                        writer.write(savedString)
                        writer.newLine()
                    }
                }
                savedString = (shapes.fill).toString()
                writer.write(savedString)
                writer.newLine()
                savedString = (shapes.stroke).toString()
                writer.write(savedString)
                writer.newLine()
                savedString = (shapes.strokeWidth).toString()
                writer.write(savedString)
                writer.newLine()
                if (shapes.strokeDashArray.isEmpty()) {
                    writer.write("0.0")
                } else {
                    savedString = (shapes.strokeDashArray[0]).toString()
                    writer.write(savedString)
                }
                writer.newLine()
            }
        }

        // Close the writer at the end
        writer.close()
    }

    private fun loadDrawing(loadFile: File?):MutableList<Shape?> {
        // read the content inside the file using buffered reader
        val reader = BufferedReader(FileReader(loadFile))

        // Record the type of the current shape
        var shapeType:String? = ""

        // Generate a new shape list
        val newShapeList:MutableList<Shape?> = mutableListOf<Shape?>()

        // Read all property of each shape and save the shape into shape list
        shapeType = reader.readLine()
        while (shapeType != null) {
            var newShape:Shape? = null
            when (shapeType) {
                "Line" -> {
                    val x1 = reader.readLine().toDouble()
                    val y1 = reader.readLine().toDouble()
                    val x2 = reader.readLine().toDouble()
                    val y2 = reader.readLine().toDouble()
                    val newLine = Line(x1, y1, x2, y2)
                    newShape = newLine
                }
                "Circle" -> {
                    val cx = reader.readLine().toDouble()
                    val cy = reader.readLine().toDouble()
                    val ra = reader.readLine().toDouble()
                    val newCircle = Circle(cx, cy, ra)
                    newShape = newCircle
                }
                "Rectangle" -> {
                    val x = reader.readLine().toDouble()
                    val y = reader.readLine().toDouble()
                    val width = reader.readLine().toDouble()
                    val height = reader.readLine().toDouble()
                    val newRectangle = Rectangle(x, y, width, height)
                    newShape = newRectangle
                }
            }
            newShape?.fill = Color.valueOf(reader.readLine())
            newShape?.stroke = Color.valueOf(reader.readLine())
            newShape?.strokeWidth = reader.readLine().toDouble()
            val element = reader.readLine().toDouble()
            if (element == 0.0) {
                newShape?.strokeDashArray?.clear()
            } else {
                newShape?.strokeDashArray?.addAll(element, element)
            }
            newShapeList.add(newShape)
            shapeType = reader.readLine()
        }
        reader.close()
        return newShapeList
    }

    // The function will put the current selected shape into text and into the clipboard
    private fun putOnClipboard() {
        // Retrieve from clipboard demo in sample code
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        model.retrieveCurrentShape()
        val currShape = model.currSelected

        // Generate shape String for representing shape
        if (currShape != null) {
            var shapeString:String = ""
            shapeString += currShape?.typeSelector + "\n"
            when (currShape?.typeSelector) {
                "Line" -> {
                    val currentLine = currShape as Line
                    shapeString += (currentLine.startX).toString() + "\n"
                    shapeString += (currentLine.startY).toString() + "\n"
                    shapeString += (currentLine.endX).toString() + "\n"
                    shapeString += (currentLine.endY).toString() + "\n"
                }
                "Circle" -> {
                    val currentCircle = currShape as Circle
                    shapeString += (currentCircle.centerX).toString() + "\n"
                    shapeString += (currentCircle.centerY).toString() + "\n"
                    shapeString += (currentCircle.radius).toString() + "\n"
                }
                "Rectangle" -> {
                    val currentRec = currShape as Rectangle
                    shapeString += (currentRec.x).toString() + "\n"
                    shapeString += (currentRec.y).toString() + "\n"
                    shapeString += (currentRec.width).toString() + "\n"
                    shapeString += (currentRec.height).toString() + "\n"
                }
            }
            shapeString += (currShape.fill).toString() + "\n"
            shapeString += (currShape.stroke).toString() + "\n"
            shapeString += (currShape.strokeWidth).toString() + "\n"
            shapeString += if (currShape.strokeDashArray.isEmpty()) {
                "0.0"
            } else {
                (currShape.strokeDashArray[0]).toString()
            }
            content.putString(shapeString)
            clipboard.setContent(content)
        }
    }

    private fun getFromClipboard():Shape? {
        val clipboard = Clipboard.getSystemClipboard()
        var shapeString = ""
        for (char in clipboard.string) {
            shapeString += char
        }
        var shapeStringList = shapeString.split("\n")
        var newShape:Shape? = null
        if (clipboard.string != "" && shapeStringList.isNotEmpty()) {
            var i = 0
            when (shapeStringList[0]) {
                "Line" -> {
                    val x1 = shapeStringList[1].toDouble()
                    val y1 = shapeStringList[2].toDouble()
                    val x2 = shapeStringList[3].toDouble()
                    val y2 = shapeStringList[4].toDouble()
                    i = 5
                    val newLine = Line(x1, y1, x2, y2)
                    newShape = newLine
                }
                "Circle" -> {
                    val cx = shapeStringList[1].toDouble()
                    val cy = shapeStringList[2].toDouble()
                    val ra = shapeStringList[3].toDouble()
                    i = 4
                    val newCircle = Circle(cx, cy, ra)
                    newShape = newCircle
                }
                "Rectangle" -> {
                    val x = shapeStringList[1].toDouble()
                    val y = shapeStringList[2].toDouble()
                    val width = shapeStringList[3].toDouble()
                    val height = shapeStringList[4].toDouble()
                    i = 5
                    val newRectangle = Rectangle(x, y, width, height)
                    newShape = newRectangle
                }
                else -> {return null}
            }
            newShape?.fill = Color.valueOf(shapeStringList[i])
            newShape?.stroke = Color.valueOf(shapeStringList[i+1])
            newShape?.strokeWidth = shapeStringList[i+2].toDouble()
            val element = shapeStringList[i+3].toDouble()
            if (element == 0.0) {
                newShape?.strokeDashArray?.clear()
            } else {
                newShape?.strokeDashArray?.addAll(element, element)
            }
        }
        return newShape
    }
}