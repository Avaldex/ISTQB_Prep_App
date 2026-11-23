# ISTQB CTFL Preparation

**An Android study app for the ISTQB Certified Tester Foundation Level (CTFL) v4.0.1 syllabus.**

Built with **Kotlin** and **Jetpack Compose** by **Vladislav Bugaev**.

> **Independent educational project.** This application is not affiliated with, sponsored by, or endorsed by ISTQB. ISTQB and CTFL are trademarks of their respective owners.

## About the project

ISTQB CTFL Preparation is designed to help aspiring software testers study the CTFL syllabus, practise exam-style questions, track learning progress, and revisit mistakes in one Android application.

The project focuses on a simple mobile experience, readable question layouts, clear answer feedback, and support for different display preferences.

## Features

### Learn and practise
- **Syllabus learning:** Study topics aligned with ISTQB CTFL v4.0.1.
- **559 syllabus practice questions:** Work through questions across the syllabus chapters.
- **Chapter tests:** Practise questions from a selected chapter.
- **Mistakes Review:** Retry questions you previously answered incorrectly. When corrected, a question is removed from future mistake-review sessions.
- **Learning statistics:** Track answered questions, correct answers, mistakes, and accuracy.

### Exam modes
- **Exam tickets:** Practise with ready-made exam sets.
- **Random Chapter Mix:** Take a 40-question exam covering multiple syllabus chapters.
- **Official Sample Exams A–D:** Four sample sets with 40 questions each (**160 questions total**), including multi-answer questions, diagrams, and tables.
- **Exam timer:** 60-minute countdown with automatic completion when time expires.
- **Passing score:** 65% (at least 26 out of 40 correct answers).
- **Answer review:** Inspect correct answers and explanations after completing an exam.

**Progress behaviour:** Syllabus-based learning, chapter tests, exam tickets, and random exams contribute to the learning statistics and Mistakes Review. Official Sample Exams A–D are kept separate from those statistics.

### Personalisation and accessibility
- Light and dark themes.
- Warm **Reading Mode**.
- High-contrast **E-Ink Mode**.
- English and Russian language support.
- Optional welcome guide with a **Show intro on startup** setting.
- Visible answer selection indicators, including support for questions requiring two answers.
- Persistent theme preferences across app restarts.

## Tech stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | Jetpack Compose |
| Local preferences | Android SharedPreferences |
| Exam data | JSON manifests and bundled image assets |
| Development environment | Android Studio |
| Version control | Git |

## Getting started

1. Clone this repository after it has been published:

   ```bash
   git clone https://github.com/Avaldex/ISTQB-CTFL-Preparation.git
   ```

2. Open the cloned project folder in **Android Studio**.
3. Allow Android Studio to sync the Gradle project.
4. Connect an Android device or start a compatible Android emulator.
5. Run the `app` configuration.

*Android SDK requirements and release APK information can be added once the publishing configuration is finalized.*

## Using the app

1. Open **Learning: Syllabus** to study a topic and practise its questions.
2. Use **Chapter Tests** to focus on individual syllabus chapters.
3. Choose **Exam Mode** for exam tickets, a random exam, or Official Sample Exams A–D.
4. Open **Statistics & Mistakes** to review progress and launch **Mistakes Review**.
5. Open the side menu to change the language or display mode.

## Testing and quality assurance

The application has been manually tested on a physical Android device.

Verified scenarios include:
- Correct-answer and incorrect-answer feedback.
- Single-answer and multi-answer selections.
- Mistakes Review updates after a question is answered correctly.
- Exam navigation, skipped questions, scoring, and answer review.
- Automatic exam completion after 60 minutes.
- Passing result at 28/40 (70%) and failing results below 65%.
- Dark theme persistence after restarting the application.
- Display readability across supported themes.
- Structural and asset-path checks for all four official sample sets.

These are the scenarios tested during development; they are not a claim of exhaustive device compatibility or automated test coverage.

## Roadmap

- [x] Syllabus learning and chapter tests
- [x] Exam practice and official sample sets
- [x] Mistakes Review and learning statistics
- [x] Light, dark, Reading, and E-Ink modes
- [x] Bilingual interface and onboarding
- [ ] Add application screenshots and a demo video to this README
- [ ] Publish an installable release build
- [ ] Expand automated testing and compatibility checks

## Project author

**Vladislav Bugaev**

- GitHub: [@Avaldex](https://github.com/Avaldex)
- LinkedIn: [Vladislav Bugaev](https://www.linkedin.com/in/vladislav-bugaev/)

## Attribution

This independent study application references the **ISTQB CTFL v4.0.1 syllabus** and its associated sample exams.

ISTQB and CTFL names and related third-party study materials belong to their respective rightsholders. Inclusion of those materials does not imply official endorsement.

---

*Learn. Practise. Review. Improve.*

**Good luck with your CTFL preparation!**
