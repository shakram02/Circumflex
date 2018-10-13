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

    init {
        decimalFormat.roundingMode = RoundingMode.CEILING
    }

    override fun call(): Void? = synchronized(lock) {
        for (url in urls) {

            val filePath = File(directoryPath, url.getFileName()).absolutePath
            val downloader = Downloader(url, filePath)
            // TODO bind the progress bar to this downloader

            downloader.progressProperty().addListener(onWorkerChangeProgress)
            downloader.onFailed = onDownloaderStateChange
            downloader.onCancelled = onDownloaderStateChange
            downloader.onRunning = onDownloaderStateChange
            downloader.onSucceeded = onDownloaderStateChange

            executor.submit(downloader)
            lock.wait() // Wait until download completes
        }

        succeeded()
        return null
    }

    private val onWorkerChangeProgress = ChangeListener<Number> { _, _, new ->
        updateProgress(new.toPercentage(), 100.0)
    }

    private val onDownloaderStateChange = EventHandler<WorkerStateEvent> {
        val stateEvent = it!!.eventType

        System.err.println("[Event] $stateEvent")

        synchronized(lock) {
            // Awake the waiting threads on termination
            when (stateEvent) {
                WorkerStateEvent.WORKER_STATE_CANCELLED -> lock.notifyAll()
                WorkerStateEvent.WORKER_STATE_FAILED -> lock.notifyAll()
                WorkerStateEvent.WORKER_STATE_SUCCEEDED -> lock.notifyAll()
            }
        }
    }

    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


