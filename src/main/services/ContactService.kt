package services

import data.ContactRepository
import data.CsvContactRepository
import models.Contact
import validation.ContactValidator
import validation.ValidationResult
import kotlin.random.Random

sealed class ServiceResult<out T> {
    data class Ok<T>(val data: T) : ServiceResult<T>()
    data class Fail(val error: String) : ServiceResult<Nothing>()
    val isSuccess: Boolean get() = this is Ok
}


class ContactService(
    private val repository: ContactRepository = CsvContactRepository()
) {
    private val contacts = mutableListOf<Contact>()

    init {
        loadFromRepository()
    }

    private fun loadFromRepository() {
        repository.loadAll().onSuccess { list ->
            contacts.clear()
            contacts.addAll(list)
        }.onFailure {
            contacts.clear()
        }
    }

    fun reload(): ServiceResult<Int> {
        return repository.loadAll().fold(
            onSuccess = { list ->
                contacts.clear()
                contacts.addAll(list)
                ServiceResult.Ok(list.size)
            },
            onFailure = { e ->
                ServiceResult.Fail("Не удалось загрузить данные: ${e.message}")
            }
        )
    }

    fun getAll(): List<Contact> = contacts.toList()

    fun getById(id: String): Contact? = contacts.find { it.id == id }

    fun create(
        firstName: String,
        lastName: String? = null,
        phone: String,
        note: String? = null
    ): ServiceResult<Contact> {
        when (val check = ContactValidator.validateContact(firstName, lastName, phone)) {
            is ValidationResult.Fail -> return ServiceResult.Fail(check.error)
            ValidationResult.Ok -> Unit
        }
        val contact = Contact(
            id = generateId(),
            firstName = firstName.trim(),
            lastName = lastName?.trim()?.takeIf { it.isNotEmpty() },
            phone = phone.trim(),
            note = note?.trim()?.takeIf { it.isNotEmpty() }
        )
        contacts.add(contact)
        val saveResult = repository.save(contact)
        if (saveResult.isFailure) {
            contacts.remove(contact)
            return ServiceResult.Fail("Не удалось сохранить контакт: ${saveResult.exceptionOrNull()?.message}")
        }
        return ServiceResult.Ok(contact)
    }

    fun update(
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        note: String? = null
    ): ServiceResult<Contact> {
        val index = contacts.indexOfFirst { it.id == id }
        if (index == -1) {
            return ServiceResult.Fail("Контакт не найден")
        }
        val current = contacts[index]
        val newFirst = firstName ?: current.firstName
        val newLast = lastName ?: current.lastName
        val newPhone = phone ?: current.phone
        when (val check = ContactValidator.validateContact(newFirst, newLast, newPhone)) {
            is ValidationResult.Fail -> return ServiceResult.Fail(check.error)
            ValidationResult.Ok -> Unit
        }
        val updated = current.copy(
            firstName = newFirst.trim(),
            lastName = newLast?.trim()?.takeIf { it.isNotEmpty() },
            phone = newPhone.trim(),
            note = note?.trim()?.takeIf { it.isNotEmpty() } ?: current.note
        )
        contacts[index] = updated
        val saveResult = repository.save(updated)
        if (saveResult.isFailure) {
            contacts[index] = current // откат
            return ServiceResult.Fail("Не удалось сохранить изменения: ${saveResult.exceptionOrNull()?.message}")
        }
        return ServiceResult.Ok(updated)
    }

    fun delete(id: String): ServiceResult<Boolean> {
        val index = contacts.indexOfFirst { it.id == id }
        if (index == -1) {
            return ServiceResult.Fail("Контакт не найден")
        }
        val removed = contacts.removeAt(index)
        val deleteResult = repository.delete(id)
        if (deleteResult.isFailure || deleteResult.getOrNull() != true) {
            contacts.add(index, removed) // откат
            return ServiceResult.Fail("Не удалось удалить контакт: ${deleteResult.exceptionOrNull()?.message ?: "ошибка записи"}")
        }
        return ServiceResult.Ok(true)
    }

    fun search(query: String): List<Contact> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return getAll()
        return contacts.filter { c ->
            c.firstName.lowercase().contains(q) ||
                (c.lastName?.lowercase()?.contains(q) == true) ||
                c.phone.lowercase().contains(q) ||
                c.fullName.lowercase().contains(q) ||
                (c.note?.lowercase()?.contains(q) == true)
        }
    }

    val count: Int get() = contacts.size

    fun clear(): ServiceResult<Unit> {
        val backup = contacts.toList()
        contacts.clear()
        val clearResult = repository.clear()
        if (clearResult.isFailure) {
            contacts.addAll(backup)
            return ServiceResult.Fail("Не удалось очистить хранилище: ${clearResult.exceptionOrNull()?.message}")
        }
        return ServiceResult.Ok(Unit)
    }

    private fun generateId(): String {
        val ts = System.currentTimeMillis()
        val rnd = Random.nextInt(10000, 99999)
        return "${ts}_$rnd"
    }
}
