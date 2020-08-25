package org.chaos

/**
 * Try monad
 */
abstract class KotlinTry<V> private constructor() {
    abstract fun isSuccess(): Boolean
    abstract fun isFailure(): Boolean

    abstract fun throwException()
    inline fun <U> map(noinline mapper: (V) -> U): KotlinTry<U> {
        return if (isFailure()) failure(failureException()) else {
            val v = successValue()
            try {
                success(mapper(v))
            } catch (e: Throwable) {
                failure(e)
            }
        }
    }

    inline fun filter(noinline predicate: (V)->Boolean): KotlinTry<V> {
        return if (isFailure()) {
            failure(failureException())
        } else {
            val v = successValue()
            try {
                return if (predicate(v)) {
                    this
                } else {
                    lift(null)
                }
            } catch (e: Throwable) {
                failure(e)
            }
        }
    }

    inline fun <U> flatMap(noinline mapper: (V)->KotlinTry<U>): KotlinTry<U> {
        return if (isFailure()) failure(failureException()) else {
            val v = successValue()
            try {
                return mapper(v)
            } catch (e: Throwable) {
                failure(e)
            }
        }
    }

    fun ifPresent(c: (V)->Nothing): KotlinTry<V> {
        if (isSuccess()) {
            c(successValue())
        }
        return this
    }

    fun ifPresentOrThrow(c: (V)->Nothing) {
        if (isSuccess()) {
            c(successValue())
        } else {
            throwException()
        }
    }

    fun ifThrowable(c: (Throwable)->Nothing): KotlinTry<V> {
        if (isFailure()) {
            c(failureException())
        }
        return this
    }

    val orThrow: V
        get() = if (isSuccess()) {
            successValue()
        } else {
            throw failureException()
        }

    fun optional(): V? {
        return if (isSuccess()) {
            successValue()
        } else {
            null
        }
    }

    fun successValue(): V {
        return (this as Success<V>).value
    }

    fun failureException(): RuntimeException {
        return (this as Failure<V>).exception
    }

    private class Success<V>(val value: V) : KotlinTry<V>() {
        override fun isSuccess(): Boolean {
            return true
        }

        override fun isFailure(): Boolean {
            return false
        }

        override fun throwException() {}
    }

    private class Failure<V> : KotlinTry<V> {
        var exception: RuntimeException

        constructor(message: String?) : super() {
            exception = IllegalStateException(message)
        }

        constructor(message: String?, e: Throwable?) : super() {
            exception = IllegalStateException(message, e)
        }

        constructor(e: Throwable?) : super() {
            exception = IllegalStateException(e)
        }

        override fun isSuccess(): Boolean {
            return false
        }

        override fun isFailure(): Boolean {
            return true
        }

        override fun throwException() {
            throw exception
        }
    }

    companion object {
        fun <V> failure(message: String?): KotlinTry<V> {
            return Failure(message)
        }

        fun <V> failure(message: String?, e: Throwable?): KotlinTry<V> {
            return Failure(message, e)
        }

        fun <V> failure(e: Throwable?): KotlinTry<V> {
            return Failure(e)
        }

        fun <V> lift(value: V?): KotlinTry<V> {
            return if (value == null) failure(NullPointerException("value must not be empty!")) else success(value)
        }

        fun <V> success(value: V): KotlinTry<V> {
            return Success(value)
        }

        fun <V> supplier(supplier: ()->V): KotlinTry<V> {
            return try {
                success(supplier())
            } catch (t: Throwable) {
                failure(t)
            }
        }

        fun <V : Collection<*>?> collMustHaveSomeOne(coll: V?): KotlinTry<V> {
            return if (coll == null || coll.size == 0) failure("size of collection must be > 0") else success(coll)
        }

        inline fun <V> test(value: V, noinline tester: (V)->Boolean): KotlinTry<V> {
            return if (tester(value)) success(value) else failure("no passed the test")
        }

        inline fun <V> test(value: V, noinline tester: (V)->Boolean, message: String?): KotlinTry<V> {
            return if (tester(value)) success(value) else failure(message)
        }

        inline fun <V, U> tried(v: V, noinline mapper: (V)->U): KotlinTry<U> {
            return try {
               success(mapper(v))
            } catch (e: Throwable) {
                failure(e)
            }
        }
    }
}
