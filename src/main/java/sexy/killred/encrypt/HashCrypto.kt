package sexy.killred.encrypt

import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object HashCrypto {

    fun String.sha1(): String = hashString("SHA-1", this)
    fun String.sha256(): String = hashString("SHA-256", this)
    fun String.sha512(): String = hashString("SHA-512", this)
    fun String.md5(): String = hashString("MD5", this)

    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        return DatatypeConverter.printHexBinary(bytes).toUpperCase()
    }
}