package models
data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() {
            val last = lastName?.trim()
            return if (last.isNullOrEmpty()) firstName.trim()
            else "${firstName.trim()} $last"
        }
}
