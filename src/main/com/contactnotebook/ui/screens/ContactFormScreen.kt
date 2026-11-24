package com.contactnotebook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import services.ContactService
import services.ServiceResult


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactFormScreen(
    service: ContactService,
    contactId: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val existing = contactId?.let { service.getById(it) }
    val isEdit = existing != null

    var firstName by remember { mutableStateOf(existing?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existing?.lastName ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Редактировать" else "Новый контакт") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = null
                    generalError = null
                },
                label = { Text("Имя *") },
                isError = firstNameError != null,
                supportingText = firstNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = null
                    generalError = null
                },
                label = { Text("Телефон *") },
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 4
            )

            if (generalError != null) {
                Text(
                    text = generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isSaving = true
                    firstNameError = null
                    phoneError = null
                    generalError = null

                    val result = if (isEdit && contactId != null) {
                        service.update(
                            id = contactId,
                            firstName = firstName,
                            lastName = lastName.ifBlank { null },
                            phone = phone,
                            note = note.ifBlank { null }
                        )
                    } else {
                        service.create(
                            firstName = firstName,
                            lastName = lastName.ifBlank { null },
                            phone = phone,
                            note = note.ifBlank { null }
                        )
                    }

                    isSaving = false
                    when (result) {
                        is ServiceResult.Ok -> onSaved()
                        is ServiceResult.Fail -> {
                            val msg = result.error
                            when {
                                msg.contains("Имя", ignoreCase = true) -> firstNameError = msg
                                msg.contains("Телефон", ignoreCase = true) ||
                                        msg.contains("телефон", ignoreCase = true) -> phoneError = msg
                                else -> generalError = msg
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Сохранение…" else "Сохранить")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        }
    }
}
