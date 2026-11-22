package com.istqb.prep.viewmodel

import android.app.Application
import android.os.SystemClock
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
    var isOfficialSampleExam by mutableStateOf(false)
        private set
    var officialExamError by mutableStateOf<String?>(null)
        private set
    var examDeadlineElapsedMs by mutableStateOf<Long?>(null)
        private set
    var examTimedOut by mutableStateOf(false)
        private set
    var selectedOptionIndices by mutableStateOf<Set<Int>>(emptySet())
        private set
    var examAnswers by mutableStateOf<List<Set<Int>>>(emptyList())
        private set
    var examReviewIndex by mutableStateOf<Int?>(null)

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
        val qs = db.questions
            .filter { it.chapter == chapter && it.source == "SYLLABUS" }
            .distinctBy { it.id }
            .shuffled()
            .map { shuffleQuestionOptions(it) }
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
        val questions = db.questions
            .filter { it.source == "SYLLABUS" && it.ticket == ticketNumber }
            .distinctBy { it.id }
            .shuffled()
            .take(40)
            .map(::shuffleQuestionOptions)
        if (questions.size != 40) {
            officialExamError = if (appLanguage == AppLanguage.EN)
                "Ticket $ticketNumber has fewer than 40 questions"
            else "В билете №$ticketNumber менее 40 вопросов"
            return
        }
        val title = if (appLanguage == AppLanguage.EN)
            "ISTQB Exam - Ticket #$ticketNumber"
        else "Экзамен ISTQB - Билет №$ticketNumber"
        launchQuiz(title, questions, examMode = true)
    }

    fun startRandomExam() {
        // Official CTFL v4.0 exam distribution: 8 + 6 + 4 + 11 + 9 + 2 = 40.
        val distribution = listOf(1 to 8, 2 to 6, 3 to 4, 4 to 11, 5 to 9, 6 to 2)
        val pools = db.questions
            .filter { it.source == "SYLLABUS" }
            .distinctBy { it.id }
            .groupBy { it.chapter }
        val questions = distribution.flatMap { (chapter, count) ->
            pools[chapter].orEmpty().shuffled().take(count)
        }.shuffled().map(::shuffleQuestionOptions)
        if (questions.size != 40) {
            officialExamError = if (appLanguage == AppLanguage.EN)
                "Not enough questions for a 40-question exam"
            else "Недостаточно вопросов для экзамена из 40 заданий"
            return
        }
        val title = if (appLanguage == AppLanguage.EN)
            "ISTQB Exam (Random Chapter Mix)"
        else "Экзамен ISTQB (Случайный микс глав)"
        launchQuiz(title, questions, examMode = true)
    }

    private val importedOfficialLetters by lazy {
        OfficialSampleRepository.availableLetters(getApplication())
    }

    fun availableOfficialSampleLetters(): Set<String> = importedOfficialLetters

    fun startOfficialSampleExam(letter: String) {
        val questions = OfficialSampleRepository.load(getApplication(), letter)
        if (questions == null) {
            officialExamError = if (appLanguage == AppLanguage.EN)
                "Sample $letter is not imported or failed validation. Run import_istqb_samples.py."
            else "Билет $letter не импортирован или не прошёл проверку. Запусти import_istqb_samples.py."
            return
        }
        val title = "ISTQB Official Sample Exam $letter"
        launchQuiz(title, questions, examMode = true, official = true)
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

    // Shuffle answers and their explanations together, preserving the correct answer.
    private fun shuffleQuestionOptions(question: Question): Question {
        val order = question.options.indices.shuffled()
        return question.copy(
            options = order.map { question.options[it] },
            correctIndex = order.indexOf(question.correctIndex),
            correctIndices = question.correctIndices.map { order.indexOf(it) },
            explanationsWrong = order.map { question.explanationsWrong.getOrElse(it) { "" } }
        )
    }

    private fun launchQuiz(
        title: String,
        questions: List<Question>,
        examMode: Boolean,
        official: Boolean = false
    ) {
        quizTitle = title
        quizQuestions = questions
        isExamMode = examMode
        isOfficialSampleExam = official
        officialExamError = null
        currentQuestionIndex = 0
        selectedOptionIndex = null
        selectedOptionIndices = emptySet()
        isAnswerSubmitted = false
        sessionCorrectCount = 0
        isQuizFinished = false
        examTimedOut = false
        examReviewIndex = null
        examAnswers = List(questions.size) { emptySet() }
        examDeadlineElapsedMs = if (examMode) SystemClock.elapsedRealtime() + 60 * 60 * 1000L else null
        currentRoute = ScreenRoute.QUIZ_RUNNER
    }

    fun selectOption(index: Int) {
        if (isAnswerSubmitted || isQuizFinished) return
        if (isExamMode && examDeadlineElapsedMs != null && timeRemainingSeconds() <= 0) {
            expireExam()
            return
        }
        selectedOptionIndex = index
        selectedOptionIndices = setOf(index)
    }

    fun toggleExamOption(index: Int) {
        if (!isExamMode || isQuizFinished) return
        if (timeRemainingSeconds() <= 0) {
            expireExam()
            return
        }
        val question = quizQuestions.getOrNull(currentQuestionIndex) ?: return
        selectedOptionIndices = if (question.allCorrectIndices.size > 1) {
            when {
                index in selectedOptionIndices -> selectedOptionIndices - index
                selectedOptionIndices.size < question.allCorrectIndices.size -> selectedOptionIndices + index
                else -> selectedOptionIndices
            }
        } else setOf(index)
        selectedOptionIndex = selectedOptionIndices.singleOrNull()
    }

    fun submitAnswer() {
        if (isExamMode) {
            advanceExamQuestion()
            return
        }
        val selected = selectedOptionIndex ?: return
        val currentQ = quizQuestions.getOrNull(currentQuestionIndex) ?: return
        val isCorrect = setOf(selected) == currentQ.allCorrectIndices
        if (isCorrect) sessionCorrectCount++
        repository.recordAnswer(currentQ.id, isCorrect)
        statsVersion++
        isAnswerSubmitted = true
    }

    fun timeRemainingSeconds(): Int {
        val deadline = examDeadlineElapsedMs ?: return 0
        return ((deadline - SystemClock.elapsedRealtime() + 999L) / 1000L)
            .coerceAtLeast(0L).toInt()
    }

    private fun advanceExamQuestion() {
        if (!isExamMode || isQuizFinished) return
        if (timeRemainingSeconds() <= 0) {
            expireExam()
            return
        }
        val currentQ = quizQuestions.getOrNull(currentQuestionIndex) ?: return
        val currentAnswer = selectedOptionIndices
        examAnswers = examAnswers.toMutableList().also { it[currentQuestionIndex] = currentAnswer }
        val isCorrect = currentAnswer == currentQ.allCorrectIndices
        if (isCorrect) sessionCorrectCount++
        // Preserve existing learning statistics for our own tickets/random exams.
        // Imported official samples never affect the 559-question progress.
        if (!isOfficialSampleExam && currentAnswer.isNotEmpty() && currentQ.source == "SYLLABUS") {
            repository.recordAnswer(currentQ.id, isCorrect)
            statsVersion++
        }
        if (currentQuestionIndex == quizQuestions.lastIndex) {
            isQuizFinished = true
            return
        }
        currentQuestionIndex++
        selectedOptionIndex = null
        selectedOptionIndices = emptySet()
    }

    fun skipExamQuestion() {
        if (isExamMode) {
            selectedOptionIndices = emptySet()
            selectedOptionIndex = null
            advanceExamQuestion()
        }
    }

    fun expireExam() {
        if (!isExamMode || isQuizFinished) return
        examTimedOut = true
        isQuizFinished = true
    }

    fun nextQuestion() {
        if (isExamMode) {
            advanceExamQuestion()
            return
        }
        if (currentQuestionIndex + 1 < quizQuestions.size) {
            currentQuestionIndex++
            selectedOptionIndex = null
            selectedOptionIndices = emptySet()
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
