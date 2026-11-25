package services

import data.ContactRepository
import models.Contact
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit-тесты ContactService с in-memory репозиторием (Day 7).
 * Не зависит от файловой системы Android.
 */
class InMemoryContactRepository : ContactRepository {
    private val store = mutableListOf<Contact>()

    override fun save(contact: Contact): Result<Unit> = runCatching {
        val i = store.indexOfFirst { it.id == contact.id }
        if (i >= 0) store[i] = contact else store.add(contact)
    }

    override fun loadAll(): Result<List<Contact>> = Result.success(store.toList())

    override fun findById(id: String): Result<Contact?> =
        Result.success(store.find { it.id == id })

    override fun delete(id: String): Result<Boolean> = runCatching {
        store.removeIf { it.id == id }
    }

    override fun search(query: String): Result<List<Contact>> = Result.success(
        store.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                it.phone.contains(query)
        }
    )

    override fun count(): Result<Int> = Result.success(store.size)

    override fun clear(): Result<Unit> = runCatching { store.clear() }
}

class ContactServiceTest {

    private lateinit var service: ContactService

    @Before
    fun setUp() {
        service = ContactService(InMemoryContactRepository())
    }

    @Test
    fun create_validContact_success() {
        val result = service.create(
            firstName = "Анна",
            lastName = "Иванова",
            phone = "+79991234567"
        )
        assertTrue(result is ServiceResult.Ok)
        assertEquals(1, service.count)
        assertEquals("Анна Иванова", (result as ServiceResult.Ok).data.fullName)
    }

    @Test
    fun create_emptyName_fail() {
        val result = service.create(firstName = "", phone = "+79991234567")
        assertTrue(result is ServiceResult.Fail)
        assertEquals(0, service.count)
    }

    @Test
    fun create_shortPhone_fail() {
        val result = service.create(firstName = "Борис", phone = "123")
        assertTrue(result is ServiceResult.Fail)
    }

    @Test
    fun search_byName_findsContact() {
        service.create(firstName = "Анна", phone = "+79991234567")
        service.create(firstName = "Борис", phone = "84951112233")
        val found = service.search("анн")
        assertEquals(1, found.size)
        assertEquals("Анна", found.first().firstName)
    }

    @Test
    fun delete_existing_success() {
        val created = service.create(firstName = "Анна", phone = "+79991234567")
        val id = (created as ServiceResult.Ok).data.id
        val del = service.delete(id)
        assertTrue(del is ServiceResult.Ok)
        assertNull(service.getById(id))
        assertEquals(0, service.count)
    }

    @Test
    fun delete_missing_fail() {
        val del = service.delete("no-such-id")
        assertTrue(del is ServiceResult.Fail)
    }

    @Test
    fun update_changesPhone() {
        val created = service.create(firstName = "Анна", phone = "+79991234567")
        val id = (created as ServiceResult.Ok).data.id
        val upd = service.update(id = id, phone = "+79997654321")
        assertTrue(upd is ServiceResult.Ok)
        assertEquals("+79997654321", service.getById(id)?.phone)
    }
}
