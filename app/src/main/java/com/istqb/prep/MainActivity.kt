package com.istqb.prep

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.istqb.prep.ui.screens.IstqbAppRoot
import com.istqb.prep.viewmodel.IstqbViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: IstqbViewModel = viewModel()
            val screenWidthDp = resources.configuration.screenWidthDp
            val isTablet = screenWidthDp >= 600 ||
                (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE

            val colorScheme = if (viewModel.isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                IstqbAppRoot(
                    viewModel = viewModel,
                    isTablet = isTablet
                )
            }
        }
    }
}
