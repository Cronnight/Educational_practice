# Архитектура ядра
## Проект: «Записная книжка контактов»
**День 3 · Исполнитель: Свиридова Диана Алексеевна**

### 1. Слои приложения

```
┌─────────────────────────────────────┐
│                 UI                  │  ← Activity, экраны (день 6)
├─────────────────────────────────────┤
│            Бизнес-логика            │  ← ContactService, валидация
├─────────────────────────────────────┤
│              Данные                 │  ← ContactService
└─────────────────────────────────────┘
```

На день 3 реализуем **только средний слой и модели**.  

### 2. Стек

- Язык: Kotlin
- Среда: Android Studio
- Платформа: Android
- БД: SQLite
- Файлы: работа с файлами CSV на Kotlin

### 3. Структура модулей (src/)

```
src/
├── models/
│   └── Contact.kt
├── validation/
│   └── ContactValidator.kt
├── services/
│   └── ContactService.kt
└── MainDemo.kt
```

### 4. Диаграмма классов

```
┌──────────────────────┐
│       Contact        │
│     (data class)     │
├──────────────────────┤
│ + id: String         │
│ + firstName: String  │
│ + lastName: String?  │
│ + phone: String      │
│ + note: String?      │
│ + createdAt: Long    │
├──────────────────────┤
│ + fullName: String   │
└──────────────────────┘
           ▲
           │ использует
┌──────────────────────┐       ┌─────────────────────────┐
│  ContactValidator    │       │    ContactService       │
│     (object)         │       ├─────────────────────────┤
├──────────────────────┤       │ - contacts: MutableList │
│ + validateName()     │       │ + create()              │
│ + validatePhone()    │◄──────│ + update()              │
│ + validateContact()  │       │ + delete()              │
└──────────────────────┘       │ + getById()             │
                               │ + getAll()              │
                               │ + search()              │
                               └─────────────────────────┘
```

### 5. Диаграммы последовательности

#### 5.1. Создание контакта

```
Пользователь → ContactService.create
                    │
                    ▼
              ContactValidator.validateContact()
                    │
           ┌────────┴────────┐
           │ ошибка          │ ок
           ▼                 ▼
      ServiceResult.Fail   создать Contact
                              │
                              ▼
                         добавить в список
                              │
                              ▼
                         ServiceResult.Ok
```

#### 5.2. Поиск контактов

```
Пользователь → ContactService.search
                    │
                    ▼
              перебрать все контакты
                    │
                    ▼
         совпадение по имени/фамилии/телефону
                    │
                    ▼
              вернуть List<Contact>
```

#### 5.3. Удаление контакта

```
Пользователь → ContactService.delete
                    │
                    ▼
              найти и удалить из списка
                    │
           ┌────────┴────────┐
           │ не найден       │ найден
           ▼                 ▼
   ServiceResult.Fail   ServiceResult.Ok
```

### 6. Решения по дизайну

| Решение | Почему |
|---------|--------|
| `data class Contact` | Автоматически equals, copy, toString |
| `ContactValidator` - object | Утилита без состояния, легко тестировать |
| Хранение в `MutableList` | SQLite и CSV - день 4-5; ядро не зависит от БД |
| `id` - UUID | Не зависит от автоинкремента БД |
| Поиск без учёта регистра | Удобнее для пользователя |
| `ServiceResult` / `ValidationResult` | Явный успех или текст ошибки |

### 7. Соответствие ТЗ (Must)

| Функция ТЗ | Где реализовано |
|------------|-----------------|
| Создать/изменить/удалить | `ContactService.create/update/delete` |
| Поля: имя, фамилия, телефон | `Contact` |
| Поиск по имени, фамилии, телефону | `ContactService.search` |
| Проверка имени и телефона | `ContactValidator` |
