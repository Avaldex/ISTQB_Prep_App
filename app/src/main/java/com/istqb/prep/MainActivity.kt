package com.istqb.prep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.WindowInfoTracker
import com.istqb.prep.ui.FoldAwareViewport
import com.istqb.prep.ui.screens.IstqbAppRoot
import com.istqb.prep.viewmodel.IstqbViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val layouts = WindowInfoTracker.getOrCreate(this)
            .windowLayoutInfo(this)
            .flowWithLifecycle(lifecycle)
        setContent {
            val viewModel: IstqbViewModel = viewModel()
            val layoutInfo by layouts.collectAsState(initial = null)
            val colorScheme = if (viewModel.isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                FoldAwareViewport(layoutInfo) { useWidePadding ->
                    IstqbAppRoot(viewModel = viewModel, isTablet = useWidePadding)
                }
            }
        }
    }
}
