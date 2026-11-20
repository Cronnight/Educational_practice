# Интеграция слоя данных
## Дни 4–5 · «Записная книжка контактов»
**Исполнитель:** Свиридова Диана Алексеевна

### 1. Выбранные механизмы интеграции

Реализован механизм хранения:

| Механизм | Класс | Файл по умолчанию | Назначение |
|----------|-------|-------------------|------------|
| CSV-файл | `CsvContactRepository` | `data/contacts.csv` | Основное хранилище |

### 2. Интерфейс слоя данных

```kotlin
interface ContactRepository {
    fun save(contact: Contact): Result<Unit>
    fun loadAll(): Result<List<Contact>>
    fun findById(id: String): Result<Contact?>
    fun delete(id: String): Result<Boolean>
    fun search(query: String): Result<List<Contact>>
    fun count(): Result<Int>
    fun clear(): Result<Unit>
}
```

Все операции возвращают `Result<T>` — явная обработка успеха/ошибки без «голых» исключений.

### 3. Связь с бизнес-логикой

`ContactService` принимает `ContactRepository` в конструкторе:

```kotlin
class ContactService(
    private val repository: ContactRepository = CsvContactRepository()
)
```

- При создании сервиса вызывается `repository.loadAll()` — данные поднимаются в память.
- После успешного `create` / `update` / `delete` изменения сразу пишутся в хранилище.
- При ошибке записи выполняется откат изменений в памяти.
- Метод `reload()` позволяет принудительно перечитать файл.

### 4. Структура файлов (src/)

```
src/
├── models/
│   └── Contact.kt
├── validation/
│   └── ContactValidator.kt
├── services/
│   └── ContactService.kt
├── data/
│   ├── ContactRepository.kt
│   └── CsvContactRepository.kt
└── MainDemo.kt
```

### 5. Работа с ресурсами

- Запись через `bufferedWriter(...).use { ... }` — гарантированное закрытие.
- Чтение через `readLines` / `readText`.
- Перед записью создаётся родительская директория.
- Проверяются права доступа.

### 6. Сценарии проверки

1. **Сохранение → перезапуск → загрузка**  
   Создаём контакты → создаём новый `ContactService` с тем же файлом → данные на месте.

2. **Пустые данные**  
   Несуществующий файл и пустой файл возвращают пустой список (не падают).

3. **Неверный формат**  
   Битый CSV → `Result.failure` с понятным сообщением.

4. **Ошибка записи**  
   При невозможности записать файл мутация откатывается, сервис возвращает `ServiceResult.Fail`.

### 7. Соответствие чек-листу

| Требование | Статус |
|------------|--------|
| Минимум один механизм интеграции | ✅ CSV |
| Данные сохраняются и загружаются после перезапуска | ✅ |
| Обработаны минимум 3 ошибочные ситуации | ✅ |
| Ядро работает с новым слоем данных | ✅ |
| Документация | ✅ integration.md + error_handling.md |
