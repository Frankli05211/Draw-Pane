## Personal Information
Boyang Li

20847992 b392li

kotlin.jvm 1.6.20

OpenJDK 17.0.2

Windows 10

## Source for images
* Selection Tool Source: https://www.flaticon.com/premium-icon/click_2767163
* Erase Tool Source: https://www.flaticon.com/free-icon/trash_3096687
* Line Tool Source: https://www.flaticon.com/premium-icon/line-segemnt_5772443
* Circle Tool Source: https://www.flaticon.com/free-icon/dry-clean_481078
* Rectangle Tool Source: https://www.flaticon.com/free-icon/rectangular-shape-outline_33848
* Fill Tool Source: https://www.flaticon.com/free-icon/color_7180272

## Special Feature implemented
1. Color choosing dialog tool
* The left color picker determines shapes' border color, while the right color picker determines shapes' fill color.
2. Fill tool
* The tool will only change the fill color of a circle or rectangle if the fill color picker is modified.
* The tool will ignore any modification to the border color picker when the selected shape is circle or rectangle.
* The tool will only change the color of a line if the border color picker is modified.
* The tool will ignore any modification to the fill color picker when the selected shape is a line.
3. Save & Load
* All save and load file will happen in the ./save directory
* Save will save the current canvas into a ".txt" file, so please name the file as {filename}.txt when saving.
* Load will load the existing ".txt" file from save directory
4. Delete shape
* The hotkey for deleting a shape is "delete" not "backspace"
5. Copy, Cut & Paste
* Additional features for this project is Copy, Cut and Paste
* When paste a saved shape, the new created shape will be at the same location as the saved shape
