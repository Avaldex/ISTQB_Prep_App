package com.istqb.prep.data

data class SyllabusDatabase(
    val version: String,
    val modules: List<StudyModule>,
    val questions: List<Question>
)

data class StudyModule(
    val id: Int,
    val chapter: Int,
    val source: String, // "SYLLABUS" or "BOOK"
    val title: String,
    val theory: String,
    val practiceQuestionId: Int,
    val language: String = "EN"
)

data class Question(
    val id: Int,
    val moduleId: Int = 0,
    val chapter: Int,
    val source: String, // "SYLLABUS" or "BOOK"
    val ticket: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationCorrect: String,
    val explanationsWrong: List<String>,
    val language: String = "EN"
)

enum class AppDisplayMode(val label: String) {
    STANDARD("Standard"),
    READING("Reading"),
    EINK("E-Ink")
}

enum class AppLanguage(val code: String, val label: String) {
    RU("RU", "Русский"),
    EN("EN", "English")
}

enum class ScreenRoute {
    HOME,
    SYLLABUS_MODULES,
    MODULE_DETAIL,
    SYLLABUS_CHAPTERS_TEST,
    BOOK_PLACEHOLDER,
    BOOK_CHAPTERS_TEST,
    EXAM_TICKETS,
    QUIZ_RUNNER,
    STATISTICS
}
