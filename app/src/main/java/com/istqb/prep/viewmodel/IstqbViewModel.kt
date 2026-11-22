package com.istqb.prep.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.istqb.prep.data.*

class IstqbViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IstqbRepository(application.applicationContext)

    var eInkFontSize by mutableStateOf(
        repository.loadEInkFontSize()
    )
        private set

    fun updateEInkFontSize(size: Int) {
        val newSize = size.coerceIn(14, 24)

        eInkFontSize = newSize
        repository.saveEInkFontSize(newSize)
    }


    private var _appLanguage = mutableStateOf(AppLanguage.EN)
    var appLanguage: AppLanguage
        get() = _appLanguage.value
        set(value) {
            _appLanguage.value = value
            db = repository.loadDatabase(value.name)
        }

    var db by mutableStateOf(repository.loadDatabase(appLanguage.name))
        private set


    private val displayPreferences = application.getSharedPreferences(
        "istqb_display_settings",
        android.content.Context.MODE_PRIVATE
    )

    private val savedDisplayMode = displayPreferences.getString(
        "display_mode",
        AppDisplayMode.STANDARD.name
    )

    private val displayModeState = mutableStateOf(
        AppDisplayMode.entries.firstOrNull {
            it.name == savedDisplayMode
        } ?: AppDisplayMode.STANDARD
    )

    var displayMode: AppDisplayMode
        get() = displayModeState.value
        set(value) {
            displayModeState.value = value

            displayPreferences.edit()
                .putString("display_mode", value.name)
                .apply()
        }

    var isDarkTheme by mutableStateOf(false)

    private val readingPreferences = application.getSharedPreferences(
        "istqb_reading_settings",
        android.content.Context.MODE_PRIVATE
    )

    private val readingModeState = mutableStateOf(
        readingPreferences.getBoolean("reading_mode", false)
    )

    var isReadingMode: Boolean
        get() = readingModeState.value
        set(value) {
            readingModeState.value = value

            readingPreferences.edit()
                .putBoolean("reading_mode", value)
                .apply()
        }

    var showIntroScreen by mutableStateOf(true)
    var currentRoute by mutableStateOf(ScreenRoute.HOME)
    var selectedModule by mutableStateOf<StudyModule?>(null)

    var quizTitle by mutableStateOf("")
    var quizQuestions by mutableStateOf<List<Question>>(emptyList())
    var isExamMode by mutableStateOf(false)
    var currentQuestionIndex by mutableStateOf(0)
    var selectedOptionIndex by mutableStateOf<Int?>(null)
    var isAnswerSubmitted by mutableStateOf(false)
    var sessionCorrectCount by mutableStateOf(0)
    var isQuizFinished by mutableStateOf(false)

    var statsVersion by mutableStateOf(0)


    fun getCorrectQuestionIds(): Set<Int> {
        statsVersion
        return repository.getCorrectIds().mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun getWrongQuestionIds(): Set<Int> {
        statsVersion
        return repository.getWrongIds().mapNotNull { it.toIntOrNull() }.toSet()
    }

    data class LearningProgress(
        val total: Int,
        val correct: Int,
        val wrong: Int
    ) {
        val answered: Int
            get() = correct + wrong

        val progressPercent: Double
            get() = if (total > 0) {
                answered * 100.0 / total
            } else 0.0

        val masteryPercent: Double
            get() = if (total > 0) {
                correct * 100.0 / total
            } else 0.0

        val accuracyPercent: Double
            get() = if (answered > 0) {
                correct * 100.0 / answered
            } else 0.0
    }

    fun getOverallSyllabusProgress(): LearningProgress {

        val allQuestionIds = db.questions
            .filter { it.source == "SYLLABUS" }
            .map { it.id }
            .toSet()

        val correctIds = getCorrectQuestionIds()
            .intersect(allQuestionIds)

        val wrongIds = getWrongQuestionIds()
            .intersect(allQuestionIds)
            .minus(correctIds)

        return LearningProgress(
            total = allQuestionIds.size,
            correct = correctIds.size,
            wrong = wrongIds.size
        )
    }

    fun getTopicProgress(module: StudyModule): LearningProgress {

        val topicQuestionIds = db.questions
            .filter {
                it.source == "SYLLABUS" &&
                        it.moduleId == module.id
            }
            .map { it.id }
            .toSet()

        val correctIds = getCorrectQuestionIds()
            .intersect(topicQuestionIds)

        val wrongIds = getWrongQuestionIds()
            .intersect(topicQuestionIds)
            .minus(correctIds)

        return LearningProgress(
            total = topicQuestionIds.size,
            correct = correctIds.size,
            wrong = wrongIds.size
        )
    }

    fun recordTopicAnswer(questionId: Int, isCorrect: Boolean) {
        repository.recordAnswer(questionId, isCorrect)
        statsVersion++
    }

    fun openModule(module: StudyModule) {
        selectedModule = module
        currentRoute = ScreenRoute.MODULE_DETAIL
    }

    fun startChapterTest(chapter: Int) {
        val qs = db.questions.filter { it.chapter == chapter && it.source == "SYLLABUS" }
        val title = if (appLanguage == AppLanguage.EN) {
            "Test: Chapter $chapter (Syllabus)"
        } else {
            "Тест: Глава $chapter (Силлабус)"
        }
        launchQuiz(title, qs, examMode = false)
    }

    fun startBookChapterTest(chapter: Int) {
        val qs = db.questions.filter { it.chapter == chapter && it.source == "BOOK" }
        val title = if (appLanguage == AppLanguage.EN) {
            "Test: Chapter $chapter (Book)"
        } else {
            "Тест: Глава $chapter (Книга)"
        }
        launchQuiz(title, qs, examMode = false)
    }

    fun startExamByTicket(ticketNumber: Int) {
        val ticketQs = db.questions.filter { it.ticket == ticketNumber }.shuffled()
        val finalQs = if (ticketQs.isNotEmpty()) ticketQs else db.questions.shuffled().distinctBy { it.id }
        val title = if (appLanguage == AppLanguage.EN) {
            "ISTQB Exam - Ticket #$ticketNumber"
        } else {
            "Экзамен ISTQB - Билет №$ticketNumber"
        }
        launchQuiz(title, finalQs, examMode = true)
    }

    fun startRandomExam() {
        val groupedByChapter = db.questions.groupBy { it.chapter }
        val balanced = mutableListOf<Question>()
        groupedByChapter.values.forEach { list ->
            balanced.addAll(list.shuffled().take(2))
        }
        val finalQs = balanced.distinctBy { it.id }.shuffled()
        val title = if (appLanguage == AppLanguage.EN) {
            "ISTQB Exam (Random Chapter Mix)"
        } else {
            "Экзамен ISTQB (Случайный микс глав)"
        }
        launchQuiz(title, finalQs, examMode = true)
    }

    fun startMistakesReview() {
        val wrongIds = getWrongQuestionIds()
        val wrongQs = db.questions.filter { it.id in wrongIds }
        if (wrongQs.isNotEmpty()) {
            val title = if (appLanguage == AppLanguage.EN) {
                "Mistakes Review (${wrongQs.size})"
            } else {
                "Работа над ошибками (${wrongQs.size})"
            }
            launchQuiz(title, wrongQs, examMode = false)
        }
    }

    fun startSinglePracticeQuestion(questionId: Int) {
        val q = db.questions.find { it.id == questionId } ?: return
        val title = if (appLanguage == AppLanguage.EN) {
            "Module Practice Question"
        } else {
            "Практическое задание к модулю"
        }
        launchQuiz(title, listOf(q), examMode = false)
    }

    private fun launchQuiz(title: String, questions: List<Question>, examMode: Boolean) {
        quizTitle = title
        quizQuestions = questions
        isExamMode = examMode
        currentQuestionIndex = 0
        selectedOptionIndex = null
        isAnswerSubmitted = false
        sessionCorrectCount = 0
        isQuizFinished = false
        currentRoute = ScreenRoute.QUIZ_RUNNER
    }

    fun selectOption(index: Int) {
        if (!isAnswerSubmitted) {
            selectedOptionIndex = index
        }
    }

    fun submitAnswer() {
        val selected = selectedOptionIndex ?: return
        val currentQ = quizQuestions.getOrNull(currentQuestionIndex) ?: return
        val isCorrect = selected == currentQ.correctIndex

        if (isCorrect) {
            sessionCorrectCount++
        }
        repository.recordAnswer(currentQ.id, isCorrect)
        statsVersion++
        isAnswerSubmitted = true
    }

    fun nextQuestion() {
        if (currentQuestionIndex + 1 < quizQuestions.size) {
            currentQuestionIndex++
            selectedOptionIndex = null
            isAnswerSubmitted = false
        } else {
            isQuizFinished = true
        }
    }

    fun resetStatistics() {
        repository.clearStats()
        statsVersion++
    }
}
