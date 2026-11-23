# ISTQB CTFL Preparation

<!-- ISTQB_RELEASE_V2_BEGIN -->
## Download APK — v2.0.0

[**Download ISTQB CTFL Preparation v2.0.0**](https://github.com/Avaldex/ISTQB_Prep_App/releases/tag/v2.0.0)

- App version: `2.0.0-ctfl-4.0.1`
- Minimum Android: 7.0 (API 24)
- Target Android: 15 (API 35)
- Signed release APK
- Successfully installed and tested on Xiaomi Redmi Note 12 Pro
- Adaptive smartphone and tablet UI
- Foldable layout support implemented and tested with previews

This is an independent educational application, not an official ISTQB application.
<!-- ISTQB_RELEASE_V2_END -->

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
   git clone https://github.com/Avaldex/ISTQB_Prep_App.git
   ```

2. Open the cloned project folder in **Android Studio**.
3. Allow Android Studio to sync the Gradle project.
4. Connect an Android device or start a compatible Android emulator.
5. Run the `app` configuration.



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

<!-- ISTQB_APP_UPDATE_2026_11_BEGIN -->
## Recent updates — November 2026

**Adaptive Android UI and foldable readiness**

- Added `FoldAwareViewport` with AndroidX WindowManager `FoldingFeature` handling. Where a separating hinge is reported, the UI is laid out inside the larger usable pane instead of drawing through the hinge.
- Wide-window spacing is enabled at approximately **600 dp** of usable pane width; the app also supports portrait, landscape and Android multi-window layouts.
- Added five Jetpack Compose previews for **320 dp, 600 dp and 840 dp** UI widths, covering the dashboard and exam-selection screens.
- Added unit tests for fold-pane calculations and an Android UI test for the study screen.
- Added a custom **red-and-gold launcher icon** (gold laurel branches, graduation cap and the words "ISTQB CTFL Preparation") with adaptive, round and legacy variants.

**Validation performed (23 November 2026)**

| Check | Result |
| --- | --- |
| `:app:assembleDebug` | Passed |
| `:app:testDebugUnitTest` | Passed (3 tests) |
| `:app:lintDebug` on JDK 21 | Completed: 0 errors, 10 warnings (mainly dependency update notices and a launcher-icon warning from before this icon was added) |
| Physical Xiaomi: portrait / landscape | Exam question, selected option and running timer preserved during rotation |
| Physical Xiaomi: split-screen | Displayed correctly in tested scenarios; exam navigation and scrolling accessible |
| Compose previews 320 / 600 / 840 dp | Dashboard and exam selection rendered; no obvious overlaps in inspected previews |
| Real foldable hinge and tablet exam flow | **Not yet tested on physical devices** |

**Build and run**

Open the project in Android Studio and run `app` on a connected Android device. In the project terminal:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

If Lint fails with a `JavaVersion.parse` error under JDK 25, use **JDK 21** to run Gradle/Lint in your terminal. The Gradle toolchain's Kotlin/JVM target remains Java 17.

**Independence notice:** This is an independent ISTQB CTFL study application. It is **not affiliated with, sponsored by, or endorsed by ISTQB®**. ISTQB is a trademark of its respective owner. Verify the licensing/attribution requirements of any official sample materials before public distribution.
<!-- ISTQB_APP_UPDATE_2026_11_END -->
