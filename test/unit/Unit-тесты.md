# Unit-тесты (Day 7)

## Файлы

- `ContactValidatorTest.kt` - валидация имени и телефона
- `ContactServiceTest.kt` - CRUD и поиск (in-memory репозиторий)

## Как запустить в Android Studio

1. Скопируйте файлы в `app/src/test/java/`
2. Убедитесь, что в `app/build.gradle.kts` есть:
   ```kotlin
   dependencies {
       testImplementation("junit:junit:4.13.2")
   }
   ```
3. ПКМ по классу теста - **Run**.
