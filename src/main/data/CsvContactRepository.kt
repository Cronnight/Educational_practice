package data

import models.Contact
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
class CsvContactRepository(
    private val filePath: String = "data/contacts.csv"
) : ContactRepository {
    private val file: File
        get() = File(filePath)
    private val separator = ";"
    init {
        ensureParentDir()
    }
    private fun ensureParentDir() {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
    }
    override fun save(contact: Contact): Result<Unit> = runCatching {
        val all = loadAllInternal().toMutableList()
        val index = all.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            all[index] = contact
        } else {
            all.add(contact)
        }
        writeAll(all)
    }
    override fun loadAll(): Result<List<Contact>> = runCatching {
        loadAllInternal()
    }
    override fun findById(id: String): Result<Contact?> = runCatching {
        loadAllInternal().find { it.id == id }
    }
    override fun delete(id: String): Result<Boolean> = runCatching {
        val all = loadAllInternal().toMutableList()
        val removed = all.removeIf { it.id == id }
        if (removed) {
            writeAll(all)
        }
        removed
    }
    override fun search(query: String): Result<List<Contact>> = runCatching {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@runCatching loadAllInternal()
        loadAllInternal().filter { c ->
            c.firstName.lowercase().contains(q) ||
                (c.lastName?.lowercase()?.contains(q) == true) ||
                c.phone.lowercase().contains(q) ||
                c.fullName.lowercase().contains(q)
        }
    }
    override fun count(): Result<Int> = runCatching {
        loadAllInternal().size
    }
    override fun clear(): Result<Unit> = runCatching {
        writeAll(emptyList())
    }
    private fun loadAllInternal(): List<Contact> {
        if (!file.exists()) {
            return emptyList()
        }
        if (!file.canRead()) {
            throw IOException("Нет прав на чтение файла: $filePath")
        }
        if (file.length() == 0L) {
            return emptyList()
        }

        val lines = file.readLines(StandardCharsets.UTF_8)
        if (lines.isEmpty()) return emptyList()

        // пропускаем заголовок, если он есть
        val dataLines = if (lines.first().startsWith("id;")) {
            lines.drop(1)
        } else {
            lines
        }

        return dataLines.mapIndexed { index, line ->
            parseLine(line, index + 1)
        }
    }
    private fun parseLine(line: String, lineNumber: Int): Contact {
        val parts = line.split(separator)
        if (parts.size < 6) {
            throw IllegalArgumentException(
                "Неверный формат строки $lineNumber: ожидалось 6 полей, получено ${parts.size}"
            )
        }
        val id = parts[0].trim()
        val firstName = parts[1].trim()
        val lastName = parts[2].trim().takeIf { it.isNotEmpty() && it != "null" }
        val phone = parts[3].trim()
        val note = parts[4].trim().takeIf { it.isNotEmpty() && it != "null" }
        val createdAt = parts[5].trim().toLongOrNull()
            ?: throw IllegalArgumentException("Неверный createdAt в строке $lineNumber")

        if (id.isEmpty() || firstName.isEmpty() || phone.isEmpty()) {
            throw IllegalArgumentException("Пустые обязательные поля в строке $lineNumber")
        }

        return Contact(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            note = note,
            createdAt = createdAt
        )
    }

    private fun writeAll(contacts: List<Contact>) {
        ensureParentDir()
        if (file.exists() && !file.canWrite()) {
            throw IOException("Нет прав на запись в файл: $filePath")
        }

        file.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.appendLine("id;firstName;lastName;phone;note;createdAt")
            for (c in contacts) {
                val last = c.lastName ?: ""
                val note = c.note ?: ""
                // экранируем ; и переносы строк в полях
                val safeFirst = escape(c.firstName)
                val safeLast = escape(last)
                val safePhone = escape(c.phone)
                val safeNote = escape(note)
                writer.appendLine(
                    "${c.id};$safeFirst;$safeLast;$safePhone;$safeNote;${c.createdAt}"
                )
            }
        }
    }

    private fun escape(value: String): String {
        return value
            .replace(";", ",")
            .replace("\n", " ")
            .replace("\r", "")
    }
}
