package circumflex

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Window
import java.io.File

class FileSelector(private val initialDirectory: String) {
    private var saveDirectory: File? = null
    private var filterDirectories: HashMap<String, File> = hashMapOf()

    fun openFile(window: Window, dialogTitle: String, extensionFilter: ExtensionFilter,
                        initialFileName: String = ""): File? {
        var latestDirectory: File? = null
        val filterDesc = extensionFilter.description

        if (filterDirectories.containsKey(filterDesc)) {
            latestDirectory = filterDirectories[filterDesc]!!
        }

        val fileChooser = setupFileChooser(dialogTitle, latestDirectory, extensionFilter)
        fileChooser.initialFileName = initialFileName

        if (!fileChooser.initialDirectory.exists()) {
            fileChooser.initialDirectory = null
        }

        val filePath = nullOnNotExist(fileChooser.showOpenDialog(window)) ?: return null

        filterDirectories[filterDesc] = filePath.parentFile
        System.err.println("[Open] $filePath")
        return filePath
    }

    fun selectSaveDirectory(window: Window, dialogTitle: String): File? {
        val directoryChooser = DirectoryChooser()
        val directoryKeyString = "dir"

        if (filterDirectories.containsKey(directoryKeyString)) {
            directoryChooser.initialDirectory =
                    getFileChooserBaseDirectory(initialDirectory, filterDirectories[directoryKeyString]!!)
        }

        directoryChooser.title = dialogTitle
        val path = directoryChooser.showDialog(window) ?: return null
        filterDirectories[directoryKeyString] = path

        return path
    }

    fun selectSavePath(window: Window, dialogTitle: String, initialName: String? = null, extensionFilter: ExtensionFilter? = null): File? {
        val fileChooser = setupFileChooser(dialogTitle, saveDirectory, extensionFilter)
        fileChooser.initialFileName = initialName
        var filePath = fileChooser.showSaveDialog(window) ?: return null

        if (extensionFilter != null) {
            // Extensions are on the form: *.xxx we don't need the *
            val extension = extensionFilter.extensions.first().replaceBefore(".", "")

            // Add the extension if it doesn't exist
            if (filePath.extension.trim().isEmpty()) {
                filePath = File(filePath.parent, "${filePath.name}$extension")
            }
        }

        System.err.println("[Save] $filePath")
        return filePath
    }

    private fun setupFileChooser(dialogTitle: String, initialPath: File?, extensionFilter: ExtensionFilter?): FileChooser {
        val fileChooser = FileChooser()

        fileChooser.title = dialogTitle
        fileChooser.initialDirectory = getFileChooserBaseDirectory(initialDirectory, initialPath)

        if (extensionFilter != null) {
            fileChooser.extensionFilters.add(extensionFilter)
        }

        return fileChooser
    }

    private fun nullOnNotExist(filePath: File?): File? {
        if (filePath == null || !filePath.exists()) {
            System.err.println("No such file [$filePath]")
            return null
        }

        return filePath
    }

    private fun getFileChooserBaseDirectory(preferredDirectory: String, lastDirectory: File? = null): File {

        if (lastDirectory != null) {
            return lastDirectory
        }

        val testPreferredDirectory = File(preferredDirectory)
        if (testPreferredDirectory.exists()) {
            return testPreferredDirectory
        }

        return File(System.getProperty("user.dir"))
    }
}