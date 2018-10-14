package sequencedownloader

import javafx.beans.value.ChangeListener
import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import java.io.File
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.Executors

/**
 * Represents a facade for download operations. Summons child downloaders for a given
 * list of URLs and monitors their state. Downloads are performed sequentially.
 */
class MultiDownloader(private val urls: List<URL>, private var directoryPath: File = File("./")) : Task<Void>(),
        Thread.UncaughtExceptionHandler {
    private val lock = Object()
    private val executor = Executors.newSingleThreadExecutor(DaemonThreadFactory(this))
    private val decimalFormat = DecimalFormat("#.##")
    private var completed = 0
    private lateinit var currentDownloader: Downloader

    init {
        decimalFormat.roundingMode = RoundingMode.CEILING
    }

    override fun call(): Void? = synchronized(lock) {
        updateMessage("Downloading...")
        for (index in urls.indices) {
            completed = index
            val url = urls[index]
            val file = File(directoryPath, url.getFileName())

            if (isPreviouslyDownloaded(url, file)) {
                updateMessage("Already downloaded: ${file.name}" +
                        "\nDownload: ${completed + 1} / ${urls.count()}")
                updateProgress(1.0, 1.0)
                Thread.sleep(SLEEP_PERIOD)
                continue
            }

            currentDownloader = Downloader(url, file.absolutePath)
            currentDownloader.progressProperty().addListener(onWorkerChangeProgress)
            currentDownloader.onFailed = onDownloaderStateChange
            currentDownloader.onCancelled = onDownloaderStateChange
            currentDownloader.onRunning = onDownloaderStateChange
            currentDownloader.onSucceeded = onDownloaderStateChange

            executor.submit(currentDownloader)
            lock.wait() // Wait until download completes
        }

        return null
    }

    private val onWorkerChangeProgress = ChangeListener<Number> { _, _, new ->
        updateProgress(new.toDouble(), 1.0)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        executor.shutdownNow()
        currentDownloader.cancel(mayInterruptIfRunning)
        updateProgress(0.0, 1.0)
        return super.cancel(mayInterruptIfRunning)
    }

    private val onDownloaderStateChange = EventHandler<WorkerStateEvent> {
        val stateEvent = it!!.eventType

        System.err.println("[Event] $stateEvent")

        synchronized(lock) {
            // Awake the waiting threads on termination
            when (stateEvent) {
                WorkerStateEvent.WORKER_STATE_RUNNING -> updateProgress(-1.0, 0.0)
                WorkerStateEvent.WORKER_STATE_CANCELLED -> lock.notifyAll()
                WorkerStateEvent.WORKER_STATE_FAILED -> lock.notifyAll()
                WorkerStateEvent.WORKER_STATE_SUCCEEDED -> {
                    updateMessage("Download: ${completed + 1} / ${urls.count()}")
                    updateProgress(1.0, 1.0)
                    lock.notifyAll()
                    Thread.sleep(SLEEP_PERIOD)
                }
            }
        }
    }

    private fun isPreviouslyDownloaded(url: URL, downloadDestination: File): Boolean {
        val openConnection = url.openConnection()
        val fileSize = openConnection.contentLengthLong

        if (downloadDestination.exists() && downloadDestination.length() == fileSize) {
            System.err.println("[Download] skip ${downloadDestination.absolutePath}, it's already downloaded")
            // TODO updateMessage() ?
            return true
        }

        return false
    }

    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val SLEEP_PERIOD = 300L
    }
}


