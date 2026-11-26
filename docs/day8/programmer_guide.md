# Руководство программиста
## Приложение «Записная книжка контактов»

**Версия:** 1.0  
**Исполнитель:** Свиридова Диана Алексеевна  
**Дата:** 25.11.2026

---

## 1. Общее описание

Мобильное Android-приложение на Kotlin + Jetpack Compose.  
Архитектура: трёхслойная (UI - Бизнес-логика - Данные).  
Хранилище: CSV-файл.

---

## 2. Стек технологий

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| IDE | Android Studio |
| minSdk | 26 |
| Хранение | CSV |
| Тесты | JUnit |
| VCS | Git |

---

## 3. Структура проекта

```
Educational_practice-day7/
├── src/
│   ├── main/
│   │   ├── com/
│   │   │   ├── MainActivity.kt
│   │   │   └── contactnotebook/
│   │   │       └── ui/
│   │   │           ├── ContactApp.kt
│   │   │           ├── screens/
│   │   │           │   ├── ContactListScreen.kt
│   │   │           │   ├── ContactFormScreen.kt
│   │   │           │   └── ContactDetailScreen.kt
│   │   │           └── theme/Theme.kt
│   │   ├── models/Contact.kt
│   │   ├── validation/ContactValidator.kt
│   │   ├── services/ContactService.kt
│   │   └── data/
│   │       ├── ContactRepository.kt
│   │       └── CsvContactRepository.kt
│   └── build.gradle.kts
├── test/unit/
│   ├── ContactValidatorTest.kt
│   └── ContactServiceTest.kt
├── docs/
├── assets/
├── .gitignore
└── README.md
```

---

## 4. Архитектура

```
┌─────────────────────────────────────┐
│                 UI                  │  Activity + Compose-экраны
├─────────────────────────────────────┤
│            Бизнес-логика            │  ContactService + ContactValidator
├─────────────────────────────────────┤
│              Данные                 │  ContactRepository + CsvContactRepository
└─────────────────────────────────────┘
```

### 4.1. Модель

```kotlin
data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String? = null,
    val phone: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 4.2. Валидация

`ContactValidator` (object) - проверяет имя и телефон.  
Возвращает `ValidationResult.Ok` или `ValidationResult.Fail(error)`.

### 4.3. Сервис

`ContactService` держит список контактов в памяти и синхронизирует его с репозиторием после каждой операции.  
Все публичные методы возвращают `ServiceResult<T>` (Ok/Fail).

### 4.4. Репозиторий

Интерфейс `ContactRepository` + реализация `CsvContactRepository`.  
Файл по умолчанию: `data/contacts.csv`.

---

## 5. Зависимости (build.gradle.kts)

Основные:

- `androidx.compose.ui`
- `androidx.compose.material3`
- `androidx.activity.compose`
- JUnit для unit-тестов

Полный список - в файле `src/build.gradle.kts`.

---

## 6. Сборка и запуск

1. Клонировать/открыть репозиторий в Android Studio.
2. Синхронизировать Gradle (`File - Sync Project with Gradle Files`).
3. Выбрать устройство/эмулятор.
4. Run - `app`.

### Сборка APK

```
Build - Build Bundle(s) / APK(s) - Build APK(s)
```

APK появится в `app/build/outputs/apk/debug/`.

---

## 7. Запуск unit-тестов

```
./gradlew test
```

или в Android Studio: правый клик по пакету `test/unit` - Run Tests.

Текущее покрытие:

- `ContactValidatorTest` - 11 тестов
- `ContactServiceTest` - 7 тестов

---

## 8. Формат CSV

Каждая строка:

```
id;firstName;lastName;phone;note;createdAt
```

- Разделитель: `;`
- Пустые поля - пустая строка между разделителями
- `createdAt` - Unix timestamp

---

## 9. Обработка ошибок

Все ошибки слоя данных заворачиваются в `Result`/`ServiceResult`.  
При ошибке записи выполняется откат изменений в памяти.  
Сообщения об ошибках - на русском языке.

Подробнее: `docs/day4/error_handling.md`.

---

## 10. Расширение функциональности

| Задача | Где менять |
|--------|------------|
| Новое поле контакта | `Contact.kt` + парсер CSV + UI-форма |
| Другое хранилище | Новая реализация `ContactRepository` |
| Сортировка списка | `ContactService.getAll()` / UI |
| Тёмная тема | `Theme.kt` + `MaterialTheme` |

---

## 11. Git-теги

| Тег | Содержание |
|-----|------------|
| day1 … day7 | Промежуточные этапы |
| v1.0 | Финальный релиз |
