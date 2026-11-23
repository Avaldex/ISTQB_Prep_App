package com.istqb.prep.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.istqb.prep.data.AppLanguage
import com.istqb.prep.data.Question
import com.istqb.prep.data.StudyModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun selectedThenCheckedAnswerSurvivesRestorationWithoutRecordingTwice() {
        val module = StudyModule(1, 1, "SYLLABUS", "Test topic", "Theory", 11)
        val questions = listOf(11, 12).map { id ->
            Question(
                id = id, moduleId = 1, chapter = 1, source = "SYLLABUS", ticket = 0,
                question = "Question $id", options = listOf("One", "Two"), correctIndex = 1,
                explanationCorrect = "Explanation", explanationsWrong = listOf("Reason", "")
            )
        }
        val recorded = mutableListOf<Pair<Int, Boolean>>()
        val restorer = StateRestorationTester(compose)
        restorer.setContent {
            MaterialTheme {
                ModuleDetailScreen(
                    strings = stringsFor(AppLanguage.EN), module = module, allQuestions = questions,
                    recordToStats = true, onToggleRecordToStats = {},
                    onRecordAnswer = { id, correct -> recorded.add(id to correct) },
                    isEInk = false, isDarkTheme = false, isReadingMode = false,
                    eInkFontSize = 18, onEInkFontSizeChange = {}, isTablet = false,
                    onStartPractice = {}, onStartChapterTest = {}
                )
            }
        }
        fun click(label: String) = compose.onNodeWithText(label).performScrollTo().performClick()
        click("A. One")
        click("Check Answer")
        click("Next Question")
        click("B. Two")

        restorer.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Topic Questions (2 of 2)").assertExists()
        compose.onNodeWithText("Check Answer").assertIsEnabled()
        click("Check Answer")

        restorer.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Correct!").assertExists()
        compose.onNodeWithText("Restart Topic Questions").assertExists()
        compose.runOnIdle { assertEquals(listOf(11 to false, 12 to true), recorded) }
    }

    @Test
    fun zoomUsesCardWidthAndSurvivesRestoration() {
        var cardWidth by mutableStateOf(240.dp)
        val restorer = StateRestorationTester(compose)
        restorer.setContent {
            MaterialTheme {
                Box(Modifier.requiredWidth(cardWidth)) {
                    PdfQuestionImage("official_samples/A/figure14.png")
                }
            }
        }
        compose.onNodeWithText("Zoom +").performClick()
        val hasPan = SemanticsMatcher("has horizontal scroll range") {
            SemanticsProperties.HorizontalScrollAxisRange in it.config
        }
        for (width in listOf(240.dp, 840.dp)) {
            compose.runOnIdle { cardWidth = width }
            val node = compose.onNode(hasPan, useUnmergedTree = true).fetchSemanticsNode()
            val panRange = node.config[SemanticsProperties.HorizontalScrollAxisRange].maxValue()
            val viewportPixels = with(compose.density) { width.toPx() }
            assertEquals(viewportPixels, panRange, 1.1f)
        }
        compose.runOnIdle { cardWidth = 240.dp }
        restorer.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Zoom −").performClick()
        compose.onNodeWithText("Zoom +").assertExists()
    }
}
