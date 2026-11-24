package validation

sealed class ValidationResult {
    object Ok : ValidationResult()
    data class Fail(val error: String) : ValidationResult()
    val isValid: Boolean get() = this is Ok
}

object ContactValidator {

    fun validateName(name: String?): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult.Fail("Имя не может быть пустым")
        }
        if (name.trim().length > 50) {
            return ValidationResult.Fail("Имя слишком длинное (макс. 50 символов)")
        }
        return ValidationResult.Ok
    }

    fun validatePhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.Fail("Телефон не может быть пустым")
        }
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) {
            return ValidationResult.Fail("В телефоне должно быть не меньше 10 цифр")
        }
        if (digits.length > 15) {
            return ValidationResult.Fail("Слишком много цифр в телефоне")
        }
        val allowed = Regex("""^[\d\s+\-()]+$""")
        if (!allowed.matches(phone.trim())) {
            return ValidationResult.Fail("Телефон содержит недопустимые символы")
        }
        return ValidationResult.Ok
    }

    fun validateLastName(lastName: String?): ValidationResult {
        if (lastName.isNullOrBlank()) return ValidationResult.Ok
        if (lastName.trim().length > 50) {
            return ValidationResult.Fail("Фамилия слишком длинная (макс. 50 символов)")
        }
        return ValidationResult.Ok
    }

    fun validateContact(
        firstName: String?,
        lastName: String? = null,
        phone: String?
    ): ValidationResult {
        validateName(firstName).let { if (it is ValidationResult.Fail) return it }
        validateLastName(lastName).let { if (it is ValidationResult.Fail) return it }
        validatePhone(phone).let { if (it is ValidationResult.Fail) return it }
        return ValidationResult.Ok
    }
}
