package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.nd.pmcburne.hwapp.one.ui.screens.MainScreen
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme

// single activity for the app. acts as the entry point
// all navigation and UI logic is handled by the MainScreen composable
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                MainScreen()
            }
        }
    }
}