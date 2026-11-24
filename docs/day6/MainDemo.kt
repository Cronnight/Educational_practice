import models.Contact
import services.ContactService
import services.ServiceResult
import validation.ContactValidator
import validation.ValidationResult

fun main() {
    println("=== Ручная проверка ядра «Записная книжка контактов» ===\n")
    val service = ContactService()
    var passed = 0
    var failed = 0
    fun check(name: String, ok: Boolean, detail: String? = null) {
        if (ok) {
            println("  [OK] $name")
            passed++
        } else {
            println("  [FAIL] $name${detail?.let { " — $it" } ?: ""}")
            failed++
        }
    }

    println("1. Валидация")
    check("пустое имя — ошибка", ContactValidator.validateName("") is ValidationResult.Fail)
    check("нормальное имя — ок", ContactValidator.validateName("Анна") is ValidationResult.Ok)
    check("телефон из 5 цифр — ошибка", ContactValidator.validatePhone("12345") is ValidationResult.Fail)
    check(
        "телефон +7 999 123-45-67 — ок",
        ContactValidator.validatePhone("+7 999 123-45-67") is ValidationResult.Ok
    )

    println("\n2. Создание контактов")
    val r1 = service.create(firstName = "Анна", lastName = "Иванова", phone = "+79991234567")
    check("создать Анну", r1.isSuccess, (r1 as? ServiceResult.Fail)?.error)
    val annaId = (r1 as? ServiceResult.Ok)?.data?.id
    val r2 = service.create(firstName = "Борис", phone = "8 (495) 111-22-33")
    check("создать Бориса без фамилии", r2.isSuccess, (r2 as? ServiceResult.Fail)?.error)
    val rBad = service.create(firstName = "", phone = "123")
    check("отклонить пустое имя", !rBad.isSuccess)
    check("в списке 2 контакта", service.count == 2)

    println("\n3. Поиск")
    val byName = service.search("анн")
    check("поиск «анн» находит Анну", byName.size == 1 && byName.first().firstName == "Анна")
    val byPhone = service.search("495")
    check("поиск «495» находит Бориса", byPhone.size == 1)
    val empty = service.search("xyz")
    check("поиск «xyz» — пусто", empty.isEmpty())

    println("\n4. Редактирование")
    if (annaId != null) {
        val upd = service.update(id = annaId, lastName = "Петрова", phone = "+79997654321")
        check("изменить фамилию и телефон Анны", upd.isSuccess)
        check(
            "фамилия стала Петрова",
            service.getById(annaId)?.lastName == "Петрова"
        )
    }

    println("\n5. Удаление")
    if (annaId != null) {
        val del = service.delete(annaId)
        check("удалить Анну", del.isSuccess)
        check("Анны больше нет", service.getById(annaId) == null)
        check("остался 1 контакт", service.count == 1)
    }
    val delMissing = service.delete("no-such-id")
    check("удаление несуществующего — ошибка", !delMissing.isSuccess)

    println("\n=== Итог: $passed ок, $failed ошибок ===")
    if (failed > 0) {
        error("Ручная проверка не пройдена")
    }
    println("Ядро работает корректно.")
}
