package circumflex

import java.util.concurrent.ThreadFactory

class DaemonThreadFactory(private val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null) : ThreadFactory {
    override fun newThread(p0: Runnable?): Thread {
        val thread = Thread(p0)
        thread.isDaemon = true

        if (uncaughtExceptionHandler != null) {
            thread.uncaughtExceptionHandler = uncaughtExceptionHandler
        }

        return thread
    }
}