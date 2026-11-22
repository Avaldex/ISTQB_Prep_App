package com.istqb.prep.data

import android.content.Context
import org.json.JSONObject

/**
 * Loads separately imported, original official sample exams.
 * The APK intentionally does not contain ISTQB's PDF content until the
 * project owner runs the local importer against PDFs from the official site.
 */
object OfficialSampleRepository {
    private val letters = setOf("A", "B", "C", "D")

    fun availableLetters(context: Context): Set<String> = letters.filterTo(mutableSetOf()) {
        load(context, it) != null
    }

    fun load(context: Context, letter: String): List<Question>? {
        val l = letter.uppercase()
        if (l !in letters) return null
        return runCatching {
            val path = "official_samples/$l/manifest.json"
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            require(root.getString("letter") == l)
            val arr = root.getJSONArray("questions")
            require(arr.length() == 40) { "Expected exactly 40 questions for $l" }
            val result = (0 until arr.length()).map { idx ->
                val row = arr.getJSONObject(idx)
                val number = row.getInt("number")
                require(number == idx + 1)
                val count = row.getInt("optionCount")
                require(count in 4..5)
                val key = row.getJSONArray("correct")
                val correct = (0 until key.length()).map { key.getInt(it) }.distinct()
                require(correct.isNotEmpty() && correct.all { it in 0 until count })
                val questionAsset = row.getString("questionImage")
                val answerAsset = row.getString("answerImage")
                // Fail closed if a crop is missing: no inaccurate or empty exam.
                context.assets.open(questionAsset).use { require(it.read() != -1) }
                context.assets.open(answerAsset).use { require(it.read() != -1) }
                // New importers can provide verified mobile-native text.
                // Older manifests and visually complex questions retain PDF crops.
                val textCandidate = row.optString("questionText", "").trim()
                val optionArray = row.optJSONArray("optionTexts")
                val optionTexts = if (optionArray != null && optionArray.length() == count) {
                    (0 until count).map { optionArray.optString(it, "").trim() }
                } else emptyList()
                val useText = row.optString("presentation", "") in setOf("text", "hybrid") &&
                    textCandidate.isNotBlank() &&
                    optionTexts.size == count && optionTexts.all { it.isNotBlank() }
                Question(
                    id = 1_000_000 + (l[0] - 'A' + 1) * 1000 + number,
                    chapter = row.optInt("chapter", 0),
                    source = "OFFICIAL_SAMPLE",
                    ticket = 0,
                    question = if (useText) textCandidate else "Question #$number",
                    options = if (useText) optionTexts else List(count) { ('A' + it).toString() },
                    correctIndex = correct.first(),
                    correctIndices = correct,
                    explanationCorrect = "Official explanation from ISTQB answer key",
                    explanationsWrong = List(count) { "" },
                    language = "EN",
                    questionImageAsset = if (useText && row.optString("presentation", "") == "text") null else questionAsset,
                    answerImageAsset = answerAsset,
                    learningObjective = row.optString("lo", ""),
                    kLevel = row.optString("kLevel", "")
                )
            }
            require(result.map { it.id }.toSet().size == 40)
            result
        }.getOrNull()
    }
}
