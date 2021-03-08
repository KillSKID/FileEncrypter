package sexy.killred.encrypt

import java.util.concurrent.Executors

object ThreadUtil {
    fun runThread(action: Runnable?) {
        service.execute(action)
    }

    private val service = Executors.newFixedThreadPool(threadCount())

    fun shutdown() {
        service.shutdownNow()
    }

    private fun threadCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}