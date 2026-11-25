package validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit-тесты ContactValidator (Day 7).
 * Запуск: из Android Studio — правый клик по классу → Run,
 * или ./gradlew test (если подключён test source set).
 */
class ContactValidatorTest {

    @Test
    fun validateName_empty_returnsFail() {
        val result = ContactValidator.validateName("")
        assertTrue(result is ValidationResult.Fail)
        assertEquals("Имя не может быть пустым", (result as ValidationResult.Fail).error)
    }

    @Test
    fun validateName_blank_returnsFail() {
        val result = ContactValidator.validateName("   ")
        assertTrue(result is ValidationResult.Fail)
    }

    @Test
    fun validateName_ok() {
        assertTrue(ContactValidator.validateName("Анна") is ValidationResult.Ok)
    }

    @Test
    fun validateName_tooLong_returnsFail() {
        val longName = "А".repeat(51)
        val result = ContactValidator.validateName(longName)
        assertTrue(result is ValidationResult.Fail)
    }

    @Test
    fun validatePhone_tooShort_returnsFail() {
        val result = ContactValidator.validatePhone("12345")
        assertTrue(result is ValidationResult.Fail)
        assertTrue((result as ValidationResult.Fail).error.contains("10"))
    }

    @Test
    fun validatePhone_exactly10digits_ok() {
        assertTrue(ContactValidator.validatePhone("9653214889") is ValidationResult.Ok)
    }

    @Test
    fun validatePhone_withPlusAndSpaces_ok() {
        assertTrue(ContactValidator.validatePhone("+7 999 123-45-67") is ValidationResult.Ok)
    }

    @Test
    fun validatePhone_tooManyDigits_returnsFail() {
        val result = ContactValidator.validatePhone("1234567890123456") // 16
        assertTrue(result is ValidationResult.Fail)
    }

    @Test
    fun validatePhone_empty_returnsFail() {
        assertTrue(ContactValidator.validatePhone("") is ValidationResult.Fail)
    }

    @Test
    fun validateContact_valid_ok() {
        val result = ContactValidator.validateContact("Анна", "Иванова", "+79991234567")
        assertTrue(result is ValidationResult.Ok)
    }

    @Test
    fun validateContact_invalidName_fails() {
        val result = ContactValidator.validateContact("", null, "+79991234567")
        assertTrue(result is ValidationResult.Fail)
    }
}
