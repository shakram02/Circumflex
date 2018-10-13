/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequencedownloader

import javafx.event.ActionEvent
import javafx.fxml.FXML
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
import java.util.concurrent.Executors

/**
 * @author mhrimaz
 */
class FXMLDocumentController : Thread.UncaughtExceptionHandler {

    @FXML
    private lateinit var status: Label
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var progress: ProgressIndicator

    private val fileSelector = FileSelector(System.getProperty("user.dir"))
    private val textFileExtensionFilter = FileChooser.ExtensionFilter("Text Files", "*.txt")
    private var templateValues: List<String>? = null
    private var evaluatedURLs = listOf<URL>()

    @FXML
    private fun handleDownloadAction(event: ActionEvent) {
        val (urls, downloadPath) = compileDownloadInformation() ?: return

        val downloader = MultiDownloader(urls, downloadPath)
        progress.progressProperty().bind(downloader.progressProperty())
        status.textProperty().bind(downloader.messageProperty())

        executor.submit(downloader)

        urlField.clear()
    }

    @FXML
    private fun loadTemplateParams(ae: ActionEvent) {
        val filePath = fileSelector.openFile(progress.scene.window,
                DIALOG_TITLE, textFileExtensionFilter, DOWNLOAD_LIST_NAME) ?: return

        try {
            templateValues = Files.readAllLines(Paths.get(filePath.absolutePath), Charset.defaultCharset())
        } catch (e: IOException) {
            System.err.println(e.localizedMessage)
            return
        }
    }

    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun compileDownloadInformation(): Pair<List<URL>, File>? {
        val urls: List<URL>
        val downloadPath: File

        if (templateValues != null) {
            evaluatedURLs = LinkEvaluator.evaluateURLs(urlField.text, templateValues!!)
            System.err.println("[URL Eval] evaluated ${evaluatedURLs.count()} URLs")

            val directoryPath = fileSelector.selectSaveDirectory(progress.scene.window,
                    "Select download folder [directory]")

            if (directoryPath == null || !directoryPath.exists() || !directoryPath.canWrite()) {
                System.err.println("[Select directory] is invalid [$directoryPath]")
                return null
            }

            urls = evaluatedURLs
            downloadPath = directoryPath
        } else {
            try {
                val url = URL(urlField.text)
                val fileName = fileSelector.selectSavePath(progress.scene.window,
                        DOWNLOAD_TO_TITLE, url.getFileName()) ?: return null

                urls = listOf(url)

                downloadPath = fileName
            } catch (e: MalformedURLException) {
                return null
            }
        }

        return Pair(urls, downloadPath)
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor(DaemonThreadFactory())
        private const val DOWNLOAD_LIST_NAME = "download_list.txt"
        private const val DIALOG_TITLE = "Load Template"
        private const val DOWNLOAD_TO_TITLE = "Download to..."
    }

}
