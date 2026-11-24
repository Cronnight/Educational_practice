package data

import models.Contact
interface ContactRepository {
    fun save(contact: Contact): Result<Unit>
   fun loadAll(): Result<List<Contact>>
    fun findById(id: String): Result<Contact?>
    fun delete(id: String): Result<Boolean>
    fun search(query: String): Result<List<Contact>>
    fun count(): Result<Int>
    fun clear(): Result<Unit>
}

sealed class DataResult<out T> {
    data class Ok<T>(val data: T) : DataResult<T>()
    data class Fail(val error: String, val cause: Throwable? = null) : DataResult<Nothing>()

    val isSuccess: Boolean get() = this is Ok
    val isFailure: Boolean get() = this is Fail

    fun getOrNull(): T? = (this as? Ok)?.data
    fun errorOrNull(): String? = (this as? Fail)?.error
}


fun <T> Result<T>.toDataResult(): DataResult<T> =
    fold(
        onSuccess = { DataResult.Ok(it) },
        onFailure = { DataResult.Fail(it.message ?: "Неизвестная ошибка", it) }
    )
