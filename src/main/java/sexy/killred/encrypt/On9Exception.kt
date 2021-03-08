package sexy.killred.encrypt

class On9Exception @JvmOverloads constructor(msg: String? = "") : RuntimeException(msg) {
    override fun toString(): String {
        return "\u5514\u597d\u9ce9\u649e\u5566\u597d\u5514\u597d\u5440"
    }

    @Synchronized
    override fun fillInStackTrace(): Throwable {
        return this
    }

    init {
        stackTrace = arrayOfNulls(0)
    }
}