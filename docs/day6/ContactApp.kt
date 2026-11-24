package com.contactnotebook.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.contactnotebook.ui.screens.ContactDetailScreen
import com.contactnotebook.ui.screens.ContactFormScreen
import com.contactnotebook.ui.screens.ContactListScreen
import services.ContactService


@Composable
fun ContactApp() {
    val navController = rememberNavController()
    // В реальном приложении сервис лучше внедрять через ViewModel / Hilt.
    // Для прототипа Day 6 создаём один экземпляр на всё приложение.
    val service = remember { ContactService() }

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            ContactListScreen(
                service = service,
                onAddClick = { navController.navigate("form") },
                onContactClick = { id -> navController.navigate("detail/$id") }
            )
        }
        composable("form") {
            ContactFormScreen(
                service = service,
                contactId = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "form/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("contactId")
            ContactFormScreen(
                service = service,
                contactId = id,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{contactId}",
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("contactId") ?: return@composable
            ContactDetailScreen(
                service = service,
                contactId = id,
                onEdit = { navController.navigate("form/$id") },
                onDeleted = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
