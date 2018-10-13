/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sequencedownloader

import javafx.application.Platform
import javafx.concurrent.WorkerStateEvent
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors

/**
 * @author mhrimaz
 * modified by shakram02
 */
class FXMLDocumentController : Thread.UncaughtExceptionHandler, Initializable {

    @FXML
    private lateinit var status: Label
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var progress: ProgressIndicator
    @FXML
    private lateinit var downloadButton: Button

    private val fileSelector = FileSelector(System.getProperty("user.dir"))
    private val textFileExtensionFilter = FileChooser.ExtensionFilter("Text Files", "*.txt")
    private var templateValues: List<String>? = null
    private var evaluatedURLs = listOf<URL>()
    private lateinit var downloader: MultiDownloader

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        val defaultUrlFile = File(DOWNLOAD_LINK_NAME)

        if (!(defaultUrlFile.exists() && defaultUrlFile.canRead())) {
            return
        }

        urlField.text = Files.readAllLines(defaultUrlFile.toPath()).first()
    }

    @FXML
    private fun handleDownloadAction(event: ActionEvent) {
        val (urls, downloadPath) = compileDownloadInformation() ?: return

        downloader = MultiDownloader(urls, downloadPath)
        progress.progressProperty().bind(downloader.progressProperty())
        status.textProperty().bind(downloader.messageProperty())

        enterDownloadState()
        executor.submit(downloader)
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
                System.err.println("[URL] ${url.toURI()}")  // This raises an exception if the URL is invalid

                val fileName = fileSelector.selectSavePath(progress.scene.window,
                        DOWNLOAD_TO_TITLE, url.getFileName()) ?: return null

                urls = listOf(url)
                downloadPath = fileName

            } catch (e: URISyntaxException) {
                val alert = Alert(Alert.AlertType.ERROR,
                        "URL isn't valid,\nDid you forgot to load substitution file?" +
                                "\nOtherwise, please enter a valid URL", ButtonType.OK)
                alert.title = "Invalid File URL"
                alert.showAndWait()
                return null
            }
        }

        return Pair(urls, downloadPath)
    }

    private fun enterDownloadState() {
        downloadButton.text = "Cancel"
        downloadButton.onAction = EventHandler<ActionEvent> { _ -> downloader.cancel(true) }
        val stateChangeHandler = EventHandler<WorkerStateEvent> { _ -> enterDownloadCompleteState() }
        downloader.onSucceeded = stateChangeHandler
        downloader.onCancelled = stateChangeHandler
        downloader.onFailed = stateChangeHandler
    }

    private fun enterDownloadCompleteState() {
        Platform.runLater {
            downloadButton.text = "Download"
            downloadButton.onAction =
                    EventHandler<ActionEvent> { _ -> this.handleDownloadAction(ActionEvent(null, null)) }
        }
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor(DaemonThreadFactory())
        private const val DOWNLOAD_LIST_NAME = "download_list.txt"
        private const val DOWNLOAD_LINK_NAME = "default_link.txt"
        private const val DIALOG_TITLE = "Load Template"
        private const val DOWNLOAD_TO_TITLE = "Download to..."
    }

}
