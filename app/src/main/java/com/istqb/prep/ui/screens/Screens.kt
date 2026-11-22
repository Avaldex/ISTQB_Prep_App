@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
package com.istqb.prep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.istqb.prep.data.AppDisplayMode
import com.istqb.prep.data.AppLanguage
import com.istqb.prep.data.Question
import com.istqb.prep.data.ScreenRoute
import com.istqb.prep.data.StudyModule
import com.istqb.prep.viewmodel.IstqbViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape


private data class UiStrings(
    val isEn: Boolean,
    val appTitle: String,
    val preparationSubtitle: String,
    val introContinue: String,
    val menuTitle: String,
    val languageLabel: String,
    val darkThemeLabel: String,
    val lightThemeText: String,
    val darkThemeText: String,
    val recordTopicStatsLabel: String,
    val recordTopicStatsOn: String,
    val recordTopicStatsOff: String,
    val back: String,
    val sectionsHeader: String,
    val item1Title: String,
    val item1Sub: String,
    val item2Title: String,
    val item2Sub: String,
    val item3Title: String,
    val item3Sub: String,
    val statsTitle: String,
    val statsSummary: (Int, Int, Int) -> String,
    val chapterPrefix: (Int) -> String,
    val questionsCount: (Int) -> String,
    val theoryHeader: String,
    val topicQuestionsHeader: (Int, Int) -> String,
    val checkAnswer: String,
    val nextQuestion: String,
    val restartTopicQuestions: String,
    val correctBadge: String,
    val wrongBadge: String,
    val wrongReasonPrefix: String,
    val openInQuiz: String,
    val chapterTestBtn: (Int) -> String,
    val randomExamBtn: String,
    val ticketTitle: (Int) -> String,
    val examHint: String,
    val resultExam: String,
    val resultQuiz: String,
    val scoreText: (Int, Int, Int) -> String,
    val passedText: String,
    val failedText: String,
    val backToMenu: String,
    val resetStats: String,
    val mistakesReview: (Int) -> String
)

private fun stringsFor(lang: AppLanguage): UiStrings = when (lang) {
    AppLanguage.EN -> UiStrings(
        isEn = true,
        appTitle = "ISTQB",
        preparationSubtitle = "Preparation",
        introContinue = "Continue",
        menuTitle = "Menu & Settings",
        languageLabel = "Language",
        darkThemeLabel = "Theme Mode",
        lightThemeText = "Light",
        darkThemeText = "Dark",
        recordTopicStatsLabel = "Record Topic Answers to Stats",
        recordTopicStatsOn = "Saving to statistics",
        recordTopicStatsOff = "Self-check mode only",
        back = "Back",
        sectionsHeader = "Sections",
        item1Title = "1) Learning: Syllabus",
        item1Sub = "ISTQB CTFL v4.0.1 syllabus topics & topic questions",
        item2Title = "2) Chapter Tests: Syllabus",
        item2Sub = "Chapter-by-chapter tests from the syllabus",
        item3Title = "3) Exam Mode",
        item3Sub = "Exam tickets & random chapter mix",
        statsTitle = "Statistics & Mistakes",
        statsSummary = { cor, err, acc -> "Correct: $cor • Mistakes: $err • Accuracy: $acc%" },
        chapterPrefix = { ch -> "Chapter $ch" },
        questionsCount = { cnt -> "Questions: $cnt" },
        theoryHeader = "Topic Text",
        topicQuestionsHeader = { cur, total -> "Topic Questions ($cur of $total)" },
        checkAnswer = "Check Answer",
        nextQuestion = "Next Question",
        restartTopicQuestions = "Restart Topic Questions",
        correctBadge = "Correct!",
        wrongBadge = "Answer Breakdown",
        wrongReasonPrefix = "Why selected option is incorrect: ",
        openInQuiz = "Open in Quiz Runner",
        chapterTestBtn = { ch -> "Chapter $ch Test" },
        randomExamBtn = "Random Exam (Chapter Mix)",
        ticketTitle = { num -> "Exam Ticket #$num" },
        examHint = "Passing score: 65%. Pick an exam ticket or start a random mix across chapters.",
        resultExam = "Exam Result",
        resultQuiz = "Test Result",
        scoreText = { cor, tot, pct -> "Correct answers: $cor of $tot ($pct%)" },
        passedText = "Status: Passed (65%+)",
        failedText = "Status: Not passed (65% required)",
        backToMenu = "Back to Menu",
        resetStats = "Reset",
        mistakesReview = { cnt -> "Mistakes Review ($cnt)" }
    )

    AppLanguage.RU -> UiStrings(
        isEn = false,
        appTitle = "ISTQB",
        preparationSubtitle = "Preparation",
        introContinue = "Продолжить",
        menuTitle = "Меню и настройки",
        languageLabel = "Язык / Language",
        darkThemeLabel = "Режим темы",
        lightThemeText = "Светлый",
        darkThemeText = "Тёмный",
        recordTopicStatsLabel = "Запись ответов из темы в статистику",
        recordTopicStatsOn = "Сохраняется в статистику",
        recordTopicStatsOff = "Режим самопроверки",
        back = "Назад",
        sectionsHeader = "Разделы",
        item1Title = "1) Обучение: Силлабус",
        item1Sub = "Темы силлабуса ISTQB v4.0.1 и вопросы после текста",
        item2Title = "2) Тест по Главам: Силлабус",
        item2Sub = "Тесты по главам силлабуса",
        item3Title = "3) Режим экзамена",
        item3Sub = "Билеты и случайный микс глав",
        statsTitle = "Статистика и ошибки",
        statsSummary = { cor, err, acc -> "Верно: $cor • Ошибок: $err • Точность: $acc%" },
        chapterPrefix = { ch -> "Глава $ch" },
        questionsCount = { cnt -> "Вопросов: $cnt" },
        theoryHeader = "Текст темы",
        topicQuestionsHeader = { cur, total -> "Вопросы по теме ($cur из $total)" },
        checkAnswer = "Проверить ответ",
        nextQuestion = "Следующий вопрос",
        restartTopicQuestions = "Пройти вопросы заново",
        correctBadge = "Верно!",
        wrongBadge = "Разбор ответа",
        wrongReasonPrefix = "Почему выбранный вариант неверен: ",
        openInQuiz = "Открыть в тренажёре",
        chapterTestBtn = { ch -> "Тест по главе $ch" },
        randomExamBtn = "Случайный экзамен (микс глав)",
        ticketTitle = { num -> "Экзаменационный билет №$num" },
        examHint = "Проходной порог: 65%. Выберите билет или запустите случайный микс по главам.",
        resultExam = "Результат экзамена",
        resultQuiz = "Результат теста",
        scoreText = { cor, tot, pct -> "Правильных ответов: $cor из $tot ($pct%)" },
        passedText = "Статус: Экзамен сдан (от 65%)",
        failedText = "Статус: Не сдано (нужно от 65%)",
        backToMenu = "Вернуться в меню",
        resetStats = "Сбросить",
        mistakesReview = { cnt -> "Работа над ошибками ($cnt)" }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepScreenOn() {
    val view = LocalView.current

    DisposableEffect(view) {
        view.keepScreenOn = true

        onDispose {
            view.keepScreenOn = false
        }
    }
}

@Composable
fun IstqbAppRoot(
    viewModel: IstqbViewModel,
    isTablet: Boolean
) {
    val s = stringsFor(viewModel.appLanguage)

    if (viewModel.showIntroScreen) {
        IntroSplashScreen(
            strings = s,
            isDarkTheme = viewModel.isDarkTheme,
            onContinue = { viewModel.showIntroScreen = false }
        )
        return
    }

    val currentRoute = viewModel.currentRoute
    if (
        currentRoute == ScreenRoute.MODULE_DETAIL ||
        currentRoute == ScreenRoute.QUIZ_RUNNER
    ) {
        KeepScreenOn()
    }
    val displayMode = viewModel.displayMode
    val isEInk = displayMode == AppDisplayMode.EINK
    val isDark = viewModel.isDarkTheme
    val readingBackground = Color(0xFFF5E9C9)
    val readingText = Color(0xFF35291C)
    val readingSurface = Color(0xFFEBDDB9)
    val backgroundColor = if (viewModel.isReadingMode) {
        readingBackground
    } else {
        MaterialTheme.colorScheme.background
    }

    val textColor = if (viewModel.isReadingMode) {
        readingText
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    var recordTopicAnswersToStats by remember { mutableStateOf(true) }

    val _statsTick = viewModel.statsVersion
    val wrongIds = viewModel.getWrongQuestionIds()
    val correctIds = viewModel.getCorrectQuestionIds()
    val totalAttempted = wrongIds.size + correctIds.size
    val accuracyPercent = if (totalAttempted > 0) (correctIds.size * 100) / totalAttempted else 0

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val topTitle = when (currentRoute) {
        ScreenRoute.HOME -> "${s.appTitle} • ${s.preparationSubtitle}"
        ScreenRoute.SYLLABUS_MODULES -> s.item1Title
        ScreenRoute.MODULE_DETAIL -> viewModel.selectedModule?.title ?: s.theoryHeader
        ScreenRoute.SYLLABUS_CHAPTERS_TEST -> s.item2Title
        ScreenRoute.BOOK_PLACEHOLDER -> s.item1Title
        ScreenRoute.BOOK_CHAPTERS_TEST -> s.item2Title
        ScreenRoute.EXAM_TICKETS -> s.item3Title
        ScreenRoute.QUIZ_RUNNER -> viewModel.quizTitle.ifBlank { s.item3Title }
        ScreenRoute.STATISTICS -> s.statsTitle
    }

    val onBack: () -> Unit = {
        viewModel.currentRoute = when (currentRoute) {
            ScreenRoute.MODULE_DETAIL -> ScreenRoute.SYLLABUS_MODULES
            ScreenRoute.QUIZ_RUNNER -> {
                if (viewModel.isExamMode) ScreenRoute.EXAM_TICKETS else ScreenRoute.HOME
            }
            else -> ScreenRoute.HOME
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == ScreenRoute.HOME,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IstqbLogoHeader(
                        title = s.appTitle,
                        subtitle = s.preparationSubtitle
                    )

                    HorizontalDivider()

                    Text(
                        text = s.menuTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.languageLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = viewModel.appLanguage == lang,
                                    onClick = { viewModel.appLanguage = lang },
                                    label = { Text(lang.code) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = s.darkThemeLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isDark) s.darkThemeText else s.lightThemeText,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.isDarkTheme = it }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (viewModel.appLanguage == AppLanguage.RU)
                                    "Режим чтения"
                                else
                                    "Reading Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (viewModel.appLanguage == AppLanguage.RU)
                                    "Тёплый фон для комфортного чтения"
                                else
                                    "Warm colors for comfortable reading",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = viewModel.isReadingMode,
                            onCheckedChange = { enabled ->
                                viewModel.isReadingMode = enabled

                                if (enabled) {
                                    viewModel.displayMode = AppDisplayMode.STANDARD
                                }
                            }
                        )
                    }

                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (viewModel.appLanguage == AppLanguage.RU)
                                    "Режим E-Ink / Kindle"
                                else
                                    "E-Ink / Kindle Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (viewModel.appLanguage == AppLanguage.RU)
                                    "Чёрно-белый режим чтения"
                                else
                                    "Black and white reading mode",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = isEInk,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    viewModel.isReadingMode = false
                                }

                                viewModel.displayMode = if (enabled) {
                                    AppDisplayMode.EINK
                                } else {
                                    AppDisplayMode.STANDARD
                                }
                            }
                        )
                    }

                    HorizontalDivider()






                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.recordTopicStatsLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (recordTopicAnswersToStats) s.recordTopicStatsOn else s.recordTopicStatsOff,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = recordTopicAnswersToStats,
                            onCheckedChange = { recordTopicAnswersToStats = it }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(


            containerColor = when {
                isEInk -> Color(0xFFF0F0F0)

                viewModel.isReadingMode &&
                        currentRoute == ScreenRoute.MODULE_DETAIL ->
                    readingBackground

                else -> MaterialTheme.colorScheme.background
            },


            topBar = {
                TopAppBar(title = {
                        Text(
                            text = topTitle,
                            maxLines = 1,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (currentRoute == ScreenRoute.HOME) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = s.menuTitle
                                )
                            }
                        } else {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = s.back
                                )
                            }
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (
                            viewModel.isReadingMode &&
                            currentRoute == ScreenRoute.MODULE_DETAIL &&
                            !isEInk
                        ) readingBackground
                        else if (isEInk) Color(0xFFF0F0F0)
                        else MaterialTheme.colorScheme.surface,

                        scrolledContainerColor = if (
                            viewModel.isReadingMode &&
                            currentRoute == ScreenRoute.MODULE_DETAIL &&
                            !isEInk
                        ) readingBackground
                        else if (isEInk) Color(0xFFF0F0F0)
                        else MaterialTheme.colorScheme.surface,

                        titleContentColor = if (
                            viewModel.isReadingMode &&
                            currentRoute == ScreenRoute.MODULE_DETAIL &&
                            !isEInk
                        ) readingText
                        else if (isEInk) Color.Black
                        else MaterialTheme.colorScheme.onSurface ,

                        navigationIconContentColor = if (
                            viewModel.isReadingMode &&
                            currentRoute == ScreenRoute.MODULE_DETAIL &&
                            !isEInk
                        ) readingText
                        else if (isEInk) Color.Black
                        else MaterialTheme.colorScheme.onSurface
                    )

                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentRoute) {
                    ScreenRoute.HOME -> HomeScreen(
                        strings = s,
                        isEInk = isEInk,
                        isTablet = isTablet,
                        correctCount = correctIds.size,
                        mistakesCount = wrongIds.size,
                        accuracyPercent = accuracyPercent,
                        onOpenStats = { viewModel.currentRoute = ScreenRoute.STATISTICS },
                        onOpenSyllabusLearning = { viewModel.currentRoute = ScreenRoute.SYLLABUS_MODULES },
                        onOpenSyllabusChapterTests = { viewModel.currentRoute = ScreenRoute.SYLLABUS_CHAPTERS_TEST },
                        onOpenExamMode = { viewModel.currentRoute = ScreenRoute.EXAM_TICKETS }
                    )

                    ScreenRoute.SYLLABUS_MODULES -> {
                        val syllabusModules = viewModel.db.modules.filter { it.source == "SYLLABUS" }
                        ModuleListScreen(
                            strings = s,
                            modules = syllabusModules,
                            isEInk = isEInk,
                            isTablet = isTablet,

                            onModuleClick = { viewModel.openModule(it) },
                            viewModel = viewModel

                        )
                    }

                    ScreenRoute.MODULE_DETAIL -> {
                        val module = viewModel.selectedModule
                        if (module != null) {

                            val moduleColors = if (viewModel.isReadingMode && !isEInk) {
                                MaterialTheme.colorScheme.copy(
                                    background = readingBackground,
                                    onBackground = readingText,
                                    surface = readingBackground,
                                    onSurface = readingText,
                                    surfaceVariant = readingSurface,
                                    onSurfaceVariant = readingText
                                )
                            } else {
                                MaterialTheme.colorScheme
                            }
                            MaterialTheme(colorScheme = moduleColors) {
                            ModuleDetailScreen(
                                strings = s,
                                module = module,
                                allQuestions = viewModel.db.questions,
                                recordToStats = recordTopicAnswersToStats,

                                eInkFontSize = viewModel.eInkFontSize,
                                onEInkFontSizeChange = viewModel::updateEInkFontSize,

                                onToggleRecordToStats = { recordTopicAnswersToStats = it },
                                onRecordAnswer = { qId, isRight ->
                                    if (recordTopicAnswersToStats) {
                                        viewModel.recordTopicAnswer(qId, isRight)
                                    }
                                },
                                isEInk = isEInk,
                                isTablet = isTablet,
                                onStartPractice = { viewModel.startSinglePracticeQuestion(module.practiceQuestionId) },
                                onStartChapterTest = { viewModel.startChapterTest(module.chapter) }
                            )}
                        } else {
                            BookPlaceholderScreen(
                                strings = s,
                                title = s.item1Title,
                                description = s.item1Sub,
                                isEInk = isEInk,
                                isTablet = isTablet,
                                onBack = { viewModel.currentRoute = ScreenRoute.SYLLABUS_MODULES }
                            )
                        }
                    }

                    ScreenRoute.SYLLABUS_CHAPTERS_TEST -> {
                        val syllabusQuestions = viewModel.db.questions.filter { it.source == "SYLLABUS" }
                        val chapters = syllabusQuestions.map { it.chapter }.distinct().sorted()
                        ChapterTestListScreen(
                            strings = s,
                            chapters = chapters,
                            questions = syllabusQuestions,
                            isEInk = isEInk,
                            isTablet = isTablet,
                            onChapterClick = { viewModel.startChapterTest(it) }
                        )
                    }

                    ScreenRoute.BOOK_PLACEHOLDER -> {
                        val bookModules = viewModel.db.modules.filter { it.source == "BOOK" }
                        if (bookModules.isNotEmpty()) {
                            ModuleListScreen(
                                strings = s,
                                modules = bookModules,
                                isEInk = isEInk,
                                isTablet = isTablet,
                                onModuleClick = { viewModel.openModule(it) }
                            )
                        } else {
                            BookPlaceholderScreen(
                                strings = s,
                                title = s.item2Title,
                                description = s.item2Sub,
                                isEInk = isEInk,
                                isTablet = isTablet,
                                onBack = { viewModel.currentRoute = ScreenRoute.HOME }
                            )
                        }
                    }

                    ScreenRoute.BOOK_CHAPTERS_TEST -> {
                        val bookQuestions = viewModel.db.questions.filter { it.source == "BOOK" }
                        val chapters = bookQuestions.map { it.chapter }.distinct().sorted()
                        ChapterTestListScreen(
                            strings = s,
                            chapters = chapters,
                            questions = bookQuestions,
                            isEInk = isEInk,
                            isTablet = isTablet,
                            onChapterClick = { viewModel.startBookChapterTest(it) }
                        )
                    }

                    ScreenRoute.EXAM_TICKETS -> {
                        val tickets = viewModel.db.questions
                            .map { it.ticket }
                            .filter { it > 0 }
                            .distinct()
                            .sorted()
                        ExamTicketsScreen(
                            strings = s,
                            tickets = tickets,
                            isEInk = isEInk,
                            isTablet = isTablet,
                            onStartRandomExam = { viewModel.startRandomExam() },
                            onStartTicket = { viewModel.startExamByTicket(it) },
                            officialReadyLetters = viewModel.availableOfficialSampleLetters(),
                            officialError = viewModel.officialExamError,
                            onStartOfficialExam = { viewModel.startOfficialSampleExam(it) }
                        )
                    }

                    ScreenRoute.QUIZ_RUNNER -> {
                        QuizRunnerScreen(
                            strings = s,
                            viewModel = viewModel,
                            isEInk = isEInk,
                            isTablet = isTablet,
                            onFinishToMenu = { viewModel.currentRoute = ScreenRoute.HOME }
                        )
                    }

                    ScreenRoute.STATISTICS -> {
                        StatisticsScreen(
                            strings = s,
                            viewModel = viewModel,
                            accuracyPercent = accuracyPercent,
                            correctCount = correctIds.size,
                            wrongIds = wrongIds,
                            isEInk = isEInk,
                            isTablet = isTablet
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroSplashScreen(
    strings: UiStrings,
    isDarkTheme: Boolean,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1200)
        onContinue()
    }

    val bg = if (isDarkTheme) Color(0xFF101318) else MaterialTheme.colorScheme.background
    val fg = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .clickable(onClick = onContinue)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IstqbLogoHeader(
                title = strings.appTitle,
                subtitle = strings.preparationSubtitle,
                textColor = fg
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onContinue) {
                Text(strings.introContinue)
            }
        }
    }
}

@Composable
private fun IstqbLogoHeader(
    title: String,
    subtitle: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = Color(0xFFB71C1C),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun HomeScreen(
    strings: UiStrings,
    isEInk: Boolean,
    isTablet: Boolean,
    correctCount: Int,
    mistakesCount: Int,
    accuracyPercent: Int,
    onOpenStats: () -> Unit,
    onOpenSyllabusLearning: () -> Unit,
    onOpenSyllabusChapterTests: () -> Unit,
    onOpenExamMode: () -> Unit
) {
    val horizontalPad = if (isTablet) 28.dp else 16.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = horizontalPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IstqbLogoHeader(
                    title = strings.appTitle,
                    subtitle = strings.preparationSubtitle
                )
            }
        }

        item {
            StyledCard(isEInk = isEInk, onClick = onOpenStats) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.statsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = strings.statsSummary(correctCount, mistakesCount, accuracyPercent),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = strings.sectionsHeader,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            MenuSectionCard(
                title = strings.item1Title,
                subtitle = strings.item1Sub,
                icon = Icons.Default.School,
                isEInk = isEInk,
                onClick = onOpenSyllabusLearning
            )
        }

        item {
            MenuSectionCard(
                title = strings.item2Title,
                subtitle = strings.item2Sub,
                icon = Icons.Default.Quiz,
                isEInk = isEInk,
                onClick = onOpenSyllabusChapterTests
            )
        }

        item {
            MenuSectionCard(
                title = strings.item3Title,
                subtitle = strings.item3Sub,
                icon = Icons.Default.Timer,
                isEInk = isEInk,
                onClick = onOpenExamMode
            )
        }
    }
}

@Composable
private fun MenuSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isEInk: Boolean,
    onClick: () -> Unit
) {
    StyledCard(isEInk = isEInk, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ModuleListScreen(
    strings: UiStrings,
    modules: List<StudyModule>,
    isEInk: Boolean,
    isTablet: Boolean,

    onModuleClick: (StudyModule) -> Unit,
    viewModel: IstqbViewModel? = null

) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = if (isTablet) 28.dp else 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modules, key = { it.id }) { module ->

            val topicProgress = if (
                module.source == "SYLLABUS" && viewModel != null
            ) {
                viewModel.getTopicProgress(module)
            } else {
                null
            }

            StyledCard(isEInk = isEInk, onClick = { onModuleClick(module) }) {
                Text(
                    text = "${strings.chapterPrefix(module.chapter)} • ${module.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = parseBoldMarkdown(module.theory),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )

                if (topicProgress != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Topic Progress: %.1f%%".format(
                            topicProgress.progressPercent
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.LightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    (topicProgress.progressPercent / 100.0)
                                        .toFloat()
                                        .coerceIn(0f, 1f)
                                )
                                .height(6.dp)
                                .background(
                                    if (isEInk) Color.Black
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Answered: ${topicProgress.answered} / ${topicProgress.total}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }
        }
    }
}

@Composable
private fun ModuleDetailScreen(
    strings: UiStrings,
    module: StudyModule,
    allQuestions: List<Question>,
    recordToStats: Boolean,
    onToggleRecordToStats: (Boolean) -> Unit,
    onRecordAnswer: (Int, Boolean) -> Unit,
    isEInk: Boolean,

    eInkFontSize: Int,
    onEInkFontSizeChange: (Int) -> Unit,

    isTablet: Boolean,
    onStartPractice: () -> Unit,
    onStartChapterTest: () -> Unit
) {



    val horizontalPad = if (isTablet) 28.dp else 16.dp
    val topicQuestions = remember(key1 = module.id, key2 = allQuestions) {
        val sameSource = allQuestions.filter { it.source == module.source }
        val explicitForModule = sameSource.filter { it.moduleId == module.id }

        val pool = if (explicitForModule.isNotEmpty()) {
            explicitForModule
        } else {
            sameSource.filter { it.moduleId == 0 && it.chapter == module.chapter }
        }

        val primary = pool.filter { it.id == module.practiceQuestionId }
        val others = pool.filter { it.id != module.practiceQuestionId }
        primary + others
    }

    var questionIndex by remember(module.id) { mutableStateOf(0) }
    var selectedOption by remember(module.id) { mutableStateOf<Int?>(null) }
    var isChecked by remember(module.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "${strings.chapterPrefix(module.chapter)} • ${module.title}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        StyledCard(isEInk = isEInk) {
            if (isEInk) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(


                        onClick = {
                            onEInkFontSizeChange(
                                (eInkFontSize - 1).coerceAtLeast(14)
                            )
                        },

                        enabled = eInkFontSize > 14,
                        modifier = Modifier.size(52.dp, 28.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.Black),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("A−", fontSize = 13.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "$eInkFontSize sp",
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(



                        onClick = {
                            onEInkFontSizeChange(
                                (eInkFontSize + 1).coerceAtMost(24)
                            )
                        },


                        enabled = eInkFontSize < 24,
                        modifier = Modifier.size(52.dp, 28.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.Black),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("A+", fontSize = 13.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Text(
                    text = strings.theoryHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            val theoryParts = remember(module.theory) {
                val normalized = module.theory.replace("\r\n", "\n")

                val parts = normalized.split(
                    Regex("""\n\s*\n"""),
                    limit = 2
                )

                val keywordPrefix = Regex(
                    """^(?:\*\*)?(?:Keywords|Ключевые слова)(?:\*\*)?\s*:\s*""",
                    RegexOption.IGNORE_CASE
                )

                val firstParagraph = parts.first().trim()

                if (
                    parts.size == 2 &&
                    keywordPrefix.containsMatchIn(firstParagraph) &&
                    parts[1].isNotBlank()
                ) {
                    Pair(
                        firstParagraph.replaceFirst(
                            keywordPrefix, ""
                        ).trim(),
                        parts[1].trimStart()
                    )
                } else {
                    Pair(null, normalized)
                }
            }

            var keywordsExpanded by remember(module.id) {
                mutableStateOf(false)
            }

            val theoryStyle = if (isEInk) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = eInkFontSize.sp,
                    lineHeight = (eInkFontSize * 1.5f).sp
                )
            } else {
                MaterialTheme.typography.bodyLarge
            }

            val keywords = theoryParts.first

            if (keywords != null) {

                val isRussian = Regex("[А-Яа-яЁё]")
                    .containsMatchIn(module.theory)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            keywordsExpanded = !keywordsExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRussian)
                            "Ключевые слова"
                        else
                            "Keywords",
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (keywordsExpanded) "▲" else "▼"
                    )
                }

                if (keywordsExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = parseBoldMarkdown(keywords),
                        style = theoryStyle
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = parseBoldMarkdown(theoryParts.second),
                style = theoryStyle
            )
        }

        if (topicQuestions.isNotEmpty()) {
            val currentQ = topicQuestions[questionIndex.coerceIn(0, topicQuestions.lastIndex)]

            Text(
                text = strings.topicQuestionsHeader(questionIndex + 1, topicQuestions.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StyledCard(isEInk = isEInk) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.recordTopicStatsLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (recordToStats) strings.recordTopicStatsOn else strings.recordTopicStatsOff,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = recordToStats,
                        onCheckedChange = onToggleRecordToStats
                    )
                }
            }

            StyledCard(isEInk = isEInk) {
                Text(
                    text = currentQ.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            currentQ.options.forEachIndexed { idx, optionText ->
                val isSelected = selectedOption == idx
                val isCorrectOption = idx == currentQ.correctIndex

                val containerColor = when {
                    !isChecked -> if (isEInk) Color.White else MaterialTheme.colorScheme.surface
                    isCorrectOption -> if (isEInk) Color(0xFFE0E0E0) else Color(0xFFDFF5E6)
                    isSelected -> if (isEInk) Color(0xFFCCCCCC) else Color(0xFFFDE2E1)
                    else -> if (isEInk) Color.White else MaterialTheme.colorScheme.surface
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isChecked) { selectedOption = idx },
                    shape = if (isEInk) RectangleShape else MaterialTheme.shapes.medium,
                    color = containerColor,
                    border = BorderStroke(
                        width = if (isEInk || isSelected) 2.dp else 1.dp,
                        color = if (isEInk) Color.Black else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + idx)}. $optionText",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (isChecked && isCorrectOption) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = strings.correctBadge)
                        } else if (isChecked && isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = strings.wrongBadge)
                        }
                    }
                }
            }

            if (!isChecked) {
                Button(
                    onClick = {
                        val chosen = selectedOption ?: return@Button
                        isChecked = true
                        onRecordAnswer(currentQ.id, chosen == currentQ.correctIndex)
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.checkAnswer)
                }
            } else {
                val isRight = selectedOption == currentQ.correctIndex
                StyledCard(isEInk = isEInk) {
                    Text(
                        text = if (isRight) strings.correctBadge else strings.wrongBadge,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentQ.explanationCorrect,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isRight && selectedOption != null) {
                        val wrongReason = currentQ.explanationsWrong.getOrNull(selectedOption!!)
                        if (!wrongReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${strings.wrongReasonPrefix}$wrongReason",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (questionIndex + 1 < topicQuestions.size) {
                    Button(
                        onClick = {
                            questionIndex++
                            selectedOption = null
                            isChecked = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.nextQuestion)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            questionIndex = 0
                            selectedOption = null
                            isChecked = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.restartTopicQuestions)
                    }
                }
            }
        }

        if (module.practiceQuestionId > 0) {
            OutlinedButton(
                onClick = onStartPractice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.openInQuiz)
            }
        }

        OutlinedButton(
            onClick = onStartChapterTest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.chapterTestBtn(module.chapter))
        }
    }
}

@Composable
fun parseBoldMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    while (cursor < text.length) {
        val start = text.indexOf("**", cursor)
        if (start == -1) {
            append(text.substring(cursor))
            break
        }
        val end = text.indexOf("**", start + 2)
        if (end == -1) {
            append(text.substring(cursor))
            break
        }
        // Обычный текст до **
        append(text.substring(cursor, start))
        // Текст внутри **...** — жирным шрифтом
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(start + 2, end))
        }
        cursor = end + 2
    }
}
@Composable
private fun ChapterTestListScreen(
    strings: UiStrings,
    chapters: List<Int>,
    questions: List<Question>,
    isEInk: Boolean,
    isTablet: Boolean,
    onChapterClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = if (isTablet) 28.dp else 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chapters) { chapter ->
            val count = questions.count { it.chapter == chapter }
            StyledCard(isEInk = isEInk, onClick = { onChapterClick(chapter) }) {
                Text(
                    text = strings.chapterPrefix(chapter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.questionsCount(count),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExamTicketsScreen(
    strings: UiStrings,
    tickets: List<Int>,
    isEInk: Boolean,
    isTablet: Boolean,
    onStartRandomExam: () -> Unit,
    onStartTicket: (Int) -> Unit,
    officialReadyLetters: Set<String>,
    officialError: String?,
    onStartOfficialExam: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = if (isTablet) 28.dp else 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StyledCard(isEInk = isEInk) {
                Text(
                    text = strings.item3Title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.examHint,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onStartRandomExam,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.randomExamBtn)
                }
            }
        }

        if (officialError != null) {
            item { Text(officialError, color = MaterialTheme.colorScheme.error) }
        }

        item {
            Text(
                if (strings.isEn) "Official ISTQB Sample Exams A–D"
                else "Официальные пробные экзамены ISTQB A–D",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(listOf("A", "B", "C", "D")) { letter ->
            val ready = letter in officialReadyLetters
            OutlinedButton(
                onClick = { onStartOfficialExam(letter) },
                enabled = ready,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sample Exam $letter · 40 " +
                    if (ready) (if (strings.isEn) "questions" else "вопросов")
                    else (if (strings.isEn) "(run PDF importer)" else "(нужен импорт PDF)"))
            }
        }
        item {
            Text(
                if (strings.isEn)
                    "Original sample exams appear here after local import from ISTQB PDFs."
                else "Оригинальные билеты появятся после локального импорта PDF ISTQB.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        items(tickets) { ticketNumber ->
            StyledCard(isEInk = isEInk, onClick = { onStartTicket(ticketNumber) }) {
                Text(
                    text = strings.ticketTitle(ticketNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PdfQuestionImage(assetPath: String) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        }.getOrNull()
    }
    if (bitmap != null) {
        var enlarged by remember(assetPath) { mutableStateOf(false) }
        val screenWidth = LocalConfiguration.current.screenWidthDp
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { enlarged = !enlarged }) {
                Text(if (enlarged) "Zoom −" else "Zoom +")
            }
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.width(if (enlarged) 700.dp else (screenWidth - 56).coerceAtLeast(240).dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    } else {
        Text("Missing PDF image: $assetPath")
    }
}

@Composable
private fun QuizRunnerScreen(
    strings: UiStrings,
    viewModel: IstqbViewModel,
    isEInk: Boolean,
    isTablet: Boolean,
    onFinishToMenu: () -> Unit
) {
    val horizontalPad = if (isTablet) 28.dp else 16.dp
    val questions = viewModel.quizQuestions
    val questionScroll = rememberScrollState()
    LaunchedEffect(viewModel.quizTitle, viewModel.currentQuestionIndex) {
        questionScroll.scrollTo(0)
    }
    var secondsLeft by remember(viewModel.quizTitle, viewModel.examDeadlineElapsedMs) {
        mutableIntStateOf(if (viewModel.isExamMode) viewModel.timeRemainingSeconds() else 0)
    }
    LaunchedEffect(viewModel.quizTitle, viewModel.examDeadlineElapsedMs, viewModel.isQuizFinished) {
        while (viewModel.isExamMode && !viewModel.isQuizFinished) {
            secondsLeft = viewModel.timeRemainingSeconds()
            if (secondsLeft <= 0) {
                viewModel.expireExam()
                break
            }
            delay(500L)
        }
    }

    if (viewModel.isQuizFinished || questions.isEmpty()) {
        val total = questions.size
        val correct = viewModel.sessionCorrectCount
        val percent = if (total > 0) (correct * 100) / total else 0
        val passed = percent >= 65

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPad, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (viewModel.isExamMode) strings.resultExam else strings.resultQuiz,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            StyledCard(isEInk = isEInk) {
                Text(
                    text = viewModel.quizTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.scoreText(correct, total, percent),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (viewModel.isExamMode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (passed) strings.passedText else strings.failedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (viewModel.isExamMode) {
                if (viewModel.examTimedOut) {
                    Text(if (strings.isEn) "Time expired" else "Время истекло")
                }
                OutlinedButton(
                    onClick = { viewModel.examReviewIndex = if (viewModel.examReviewIndex == null) 0 else null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (viewModel.examReviewIndex == null)
                        (if (strings.isEn) "Review answers" else "Разбор ответов")
                        else (if (strings.isEn) "Hide review" else "Скрыть разбор"))
                }
                val idx = viewModel.examReviewIndex
                if (idx != null && idx in questions.indices) {
                    val reviewQuestion = questions[idx]
                    val given = viewModel.examAnswers.getOrElse(idx) { emptySet() }
                    StyledCard(isEInk = isEInk) {
                        Text("${idx + 1} / ${questions.size}", fontWeight = FontWeight.Bold)
                        Text(
                            (if (strings.isEn) "Your answer: " else "Твой ответ: ") +
                            (if (given.isEmpty()) "—" else given.sorted().joinToString(", ") { ('A' + it).toString() })
                        )
                        Text(
                            (if (strings.isEn) "Correct: " else "Правильно: ") +
                            reviewQuestion.allCorrectIndices.sorted().joinToString(", ") { ('A' + it).toString() }
                        )
                        if (reviewQuestion.answerImageAsset != null) {
                            PdfQuestionImage(reviewQuestion.answerImageAsset)
                        } else {
                            Text(reviewQuestion.question)
                            Text(reviewQuestion.explanationCorrect)
                        }
                        if (!reviewQuestion.kLevel.isNullOrBlank()) {
                            Text("${reviewQuestion.kLevel}  ${reviewQuestion.learningObjective.orEmpty()}")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.examReviewIndex = idx - 1 },
                            enabled = idx > 0,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (strings.isEn) "Previous" else "Назад") }
                        OutlinedButton(
                            onClick = { viewModel.examReviewIndex = idx + 1 },
                            enabled = idx < questions.lastIndex,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (strings.isEn) "Next" else "Далее") }
                    }
                }
            }
            OutlinedButton(
                onClick = onFinishToMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.backToMenu)
            }
        }
        return
    }

    val question = questions[viewModel.currentQuestionIndex]
    val selected = viewModel.selectedOptionIndex
    val submitted = viewModel.isAnswerSubmitted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(questionScroll)
            .padding(horizontal = horizontalPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${viewModel.currentQuestionIndex + 1} / ${questions.size}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (viewModel.isExamMode) {
            Text(
                text = String.format(Locale.ROOT, "%02d:%02d", secondsLeft / 60, secondsLeft % 60),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        StyledCard(isEInk = isEInk) {
            // Hybrid official questions: readable text + only the real table/diagram.
            // Legacy PDF-only questions retain the original image and letter choices.
            val hasNativeText = question.questionImageAsset == null ||
                !question.question.matches(Regex("Question #\\d+"))
            if (hasNativeText) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            question.questionImageAsset?.let { path ->
                if (hasNativeText) Spacer(modifier = Modifier.height(12.dp))
                PdfQuestionImage(path)
            }
        }
        if (viewModel.isExamMode && question.allCorrectIndices.size > 1) {
            Text(if (strings.isEn) "Select ${question.allCorrectIndices.size} answers"
                else "Выбери ${question.allCorrectIndices.size} варианта")
        }

        question.options.forEachIndexed { index, optionText ->
            val isSelected = if (viewModel.isExamMode)
                index in viewModel.selectedOptionIndices else selected == index
            val isCorrectOption = index in question.allCorrectIndices

            val containerColor = when {
                !submitted -> if (isEInk) Color.White else MaterialTheme.colorScheme.surface
                isCorrectOption -> if (isEInk) Color(0xFFE0E0E0) else Color(0xFFDFF5E6)
                isSelected -> if (isEInk) Color(0xFFCCCCCC) else Color(0xFFFDE2E1)
                else -> if (isEInk) Color.White else MaterialTheme.colorScheme.surface
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !submitted) {
                        if (viewModel.isExamMode) viewModel.toggleExamOption(index)
                        else viewModel.selectOption(index)
                    },
                shape = if (isEInk) RectangleShape else MaterialTheme.shapes.medium,
                color = containerColor,
                border = BorderStroke(
                    width = if (isEInk || isSelected) 2.dp else 1.dp,
                    color = if (isEInk) Color.Black else MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (optionText == ('A' + index).toString())
                            optionText
                        else "${('A' + index)}. $optionText",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (submitted && isCorrectOption) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = strings.correctBadge)
                    } else if (submitted && isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = strings.wrongBadge)
                    }
                }
            }
        }

        if (viewModel.isExamMode) {
            Button(
                onClick = { viewModel.submitAnswer() },
                enabled = viewModel.selectedOptionIndices.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.currentQuestionIndex == questions.lastIndex)
                    (if (strings.isEn) "Finish exam" else "Завершить экзамен")
                    else strings.nextQuestion)
            }
            OutlinedButton(
                onClick = { viewModel.skipExamQuestion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (strings.isEn) "Skip question" else "Пропустить вопрос")
            }
        } else if (!submitted) {
            Button(
                onClick = { viewModel.submitAnswer() },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.checkAnswer)
            }
        } else {
            val isCorrect = selected == question.correctIndex
            StyledCard(isEInk = isEInk) {
                Text(
                    text = if (isCorrect) strings.correctBadge else strings.wrongBadge,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = question.explanationCorrect,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!isCorrect && selected != null) {
                    val wrongReason = question.explanationsWrong.getOrNull(selected)
                    if (!wrongReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${strings.wrongReasonPrefix}$wrongReason",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.nextQuestion)
            }
        }
    }
}

@Composable
private fun StatisticsScreen(
    strings: UiStrings,
    viewModel: IstqbViewModel,
    accuracyPercent: Int,
    correctCount: Int,
    wrongIds: Set<Int>,
    isEInk: Boolean,
    isTablet: Boolean
) {
    val horizontalPad = if (isTablet) 28.dp else 16.dp
    val wrongQuestions = viewModel.db.questions.filter { it.id in wrongIds }
    val overallProgress = viewModel.getOverallSyllabusProgress()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = horizontalPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StyledCard(isEInk = isEInk) {
                Text(
                    text = "Overall Syllabus Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Progress: %.1f%%".format(
                        overallProgress.progressPercent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                (overallProgress.progressPercent / 100.0)
                                    .toFloat()
                                    .coerceIn(0f, 1f)
                            )
                            .height(8.dp)
                            .background(
                                if (isEInk) Color.Black
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Answered: ${overallProgress.answered} / ${overallProgress.total}"
                )

                Text(
                    text = "Mastery: %.1f%%".format(
                        overallProgress.masteryPercent
                    )
                )

                Text(
                    text = "Correct: ${overallProgress.correct}  •  Mistakes: ${overallProgress.wrong}"
                )
            }
        }

        item {
            StyledCard(isEInk = isEInk) {
                Text(
                    text = strings.statsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.statsSummary(correctCount, wrongIds.size, accuracyPercent),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (wrongQuestions.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.startMistakesReview() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.mistakesReview(wrongQuestions.size))
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.resetStatistics() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.resetStats)
                    }
                }
            }
        }

        if (wrongQuestions.isNotEmpty()) {
            items(wrongQuestions, key = { it.id }) { q ->
                StyledCard(isEInk = isEInk) {
                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = q.explanationCorrect,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BookPlaceholderScreen(
    strings: UiStrings,
    title: String,
    description: String,
    isEInk: Boolean,
    isTablet: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isTablet) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        StyledCard(isEInk = isEInk) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) {
                Text(strings.backToMenu)
            }
        }
    }
}

@Composable
private fun StyledCard(
    isEInk: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = if (isEInk) RectangleShape else MaterialTheme.shapes.medium
    val containerColor = if (isEInk) Color.White else MaterialTheme.colorScheme.surface
    val border = if (isEInk) BorderStroke(2.dp, Color.Black) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = if (isEInk) Color.Black else MaterialTheme.colorScheme.onSurface
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = if (isEInk) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = if (isEInk) Color.Black else MaterialTheme.colorScheme.onSurface
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = if (isEInk) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}
