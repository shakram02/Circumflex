/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequencedownloader

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.stage.FileChooser

import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ResourceBundle
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author mhrimaz
 */
class FXMLDocumentController : Initializable {

    @FXML
    private val status: Label? = null
    @FXML
    private val urlField: TextField? = null
    @FXML
    private val fileField: TextField? = null
    @FXML
    private val progress: ProgressIndicator? = null

    private val fileSelector = FileSelector(System.getProperty("user.dir"))
    private val textFileExtensionFilter = FileChooser.ExtensionFilter("Text Files", "*.txt")

    @FXML
    private fun handleDownloadAction(event: ActionEvent) {
        try {
            val url = URL(urlField!!.text)
            val filename = fileField!!.text
            val downloader = Downloader(url, filename)
            progress!!.progressProperty().bind(downloader.progressProperty())
            status!!.textProperty().bind(downloader.messageProperty())
            executor.submit(downloader)
        } catch (ex: MalformedURLException) {
            Logger.getLogger(FXMLDocumentController::class.java.name).log(Level.SEVERE, null, ex)
        } finally {
            fileField!!.clear()
            urlField!!.clear()
        }
    }

    override fun initialize(url: URL, rb: ResourceBundle) {

    }

    @FXML
    private fun loadTemplateParams(ae: ActionEvent) {
        val filePath = fileSelector.openFile(progress!!.scene.window,
                "Load template", textFileExtensionFilter) ?: return

        val templateValues: List<String>
        try {
            templateValues = Files.readAllLines(Paths.get(filePath.absolutePath), Charset.defaultCharset())
        } catch (e: IOException) {
            System.err.println(e.localizedMessage)
            return
        }

        //        ArrayList<String> evaluatedLinks =
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }

}
