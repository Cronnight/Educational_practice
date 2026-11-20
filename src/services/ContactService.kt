package services

import models.Contact
import validation.ContactValidator
import validation.ValidationResult
import kotlin.random.Random

sealed class ServiceResult<out T> {
    data class Ok<T>(val data: T) : ServiceResult<T>()
    data class Fail(val error: String) : ServiceResult<Nothing>()
    val isSuccess: Boolean get() = this is Ok
}

class ContactService {
    private val contacts = mutableListOf<Contact>()

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
        return ServiceResult.Ok(updated)
    }

    fun delete(id: String): ServiceResult<Boolean> {
        val removed = contacts.removeIf { it.id == id }
        return if (removed) ServiceResult.Ok(true)
        else ServiceResult.Fail("Контакт не найден")
    }

    fun search(query: String): List<Contact> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return getAll()
        return contacts.filter { c ->
            c.firstName.lowercase().contains(q) ||
                (c.lastName?.lowercase()?.contains(q) == true) ||
                c.phone.lowercase().contains(q) ||
                c.fullName.lowercase().contains(q)
        }
    }

    val count: Int get() = contacts.size

    fun clear() = contacts.clear()
    private fun generateId(): String {
        val ts = System.currentTimeMillis()
        val rnd = Random.nextInt(10000, 99999)
        return "${ts}_$rnd"
    }
}
