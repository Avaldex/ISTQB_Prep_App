# ISTQB CTFL Preparation

**Независимое Android-приложение для подготовки к экзамену ISTQB Certified Tester Foundation Level (CTFL) v4.0.1.**

[English README](README.md) · [Исходный код](https://github.com/Avaldex/ISTQB_Prep_App) · [Все релизы](https://github.com/Avaldex/ISTQB_Prep_App/releases)

## Скачать APK — v2.0.1

**[Скачать ISTQB CTFL Preparation v2.0.1](https://github.com/Avaldex/ISTQB_Prep_App/releases/tag/v2.0.1)**

- Версия приложения: `2.0.0-ctfl-4.0.1` (versionCode `2`)
- Минимальная версия: Android 7.0 (API 24)
- Целевая версия: Android 15 (API 35)
- APK подписан цифровой подписью (APK Signature Scheme v2)
- Release APK установлен и проверен на Xiaomi Redmi Note 12 Pro

Для установки скачайте APK со страницы релиза и откройте его на совместимом Android-устройстве. При необходимости разрешите установку приложений из выбранного источника.

## О проекте

Приложение помогает изучать программу CTFL, тренироваться на вопросах, проходить пробные экзамены, анализировать ошибки и отслеживать прогресс обучения.

## Возможности

- Обучение по темам программы **CTFL v4.0.1**.
- **559 тренировочных вопросов** по темам программы и тесты по главам.
- Экзаменационные билеты и случайный экзамен из вопросов разных глав.
- **Официальные пробные экзамены ISTQB A–D.**
- Экзаменационный таймер на **60 минут** и проходной порог **65%**.
- Статистика ответов и отдельная тренировка ошибок.
- Результаты официальных Sample Exams A–D не включаются в общую статистику программы.
- Интерфейс на русском и английском языках.
- Светлая, тёмная темы, режим чтения и E-Ink Mode.
- Собственная красно-золотая иконка с лавровым венком.

## Адаптивный интерфейс

Интерфейс разработан на **Jetpack Compose** для смартфонов и планшетов, с поддержкой смены ориентации и адаптации к разделённым экранам складных устройств. Используется `FoldAwareViewport` на базе AndroidX WindowManager.

Для телефонных и планшетных размеров подготовлены Compose Preview. Сценарии со складными экранами проверялись программно и через Preview; тестирование на физическом Fold/Flip не заявляется.

## Технологии

| Компонент | Технология |
| --- | --- |
| Язык | Kotlin |
| Интерфейс | Jetpack Compose, Material Design |
| Адаптивность | AndroidX WindowManager |
| Сборка | Gradle / Android Studio |
| Данные вопросов | JSON-манифесты и локальные ресурсы |
| Контроль версий | Git / GitHub |

## Проверки

- Успешная сборка Debug и Release.
- Проверены тесты и адаптивные предпросмотры интерфейса.
- Release APK успешно установлен и запущен на **Xiaomi Redmi Note 12 Pro**; основные функции проверены на устройстве.
- Подпись Release APK проверена инструментом `apksigner`.

## Сборка из исходного кода

```bash
git clone https://github.com/Avaldex/ISTQB_Prep_App.git
```

Откройте проект в Android Studio, дождитесь Gradle Sync и запустите конфигурацию `app` на совместимом Android-устройстве или эмуляторе.

## Авторство и правовая информация

**Независимый образовательный проект.** Приложение не связано с ISTQB, не спонсируется и не одобрено ISTQB. ISTQB и CTFL являются товарными знаками соответствующих правообладателей. Права и условия использования сторонних учебных материалов сохраняются за их правообладателями.

<!-- ISTQB_GALLERY_BEGIN -->
## Скриншоты
Реальные скриншоты Android-приложения, включая четыре режима оформления. Для увеличения откройте изображение на GitHub.

### Начало работы
<p align="center">
  <img src="docs/screenshots/welcome/01_welcome_light.png" alt="Стартовая инструкция" width="218" />
  <img src="docs/screenshots/home/01_home_light.png" alt="Главное меню — светлая тема" width="218" />
  <img src="docs/screenshots/learning/01_syllabus_topics_light.png" alt="Список тем силлабуса" width="218" />
</p>

### Обучение и тесты по главам
<p align="center">
  <img src="docs/screenshots/learning/02_topic_theory_light.png" alt="Теория и ключевые слова" width="218" />
  <img src="docs/screenshots/learning/03_answer_selected_light.png" alt="Выбранный ответ — светлая тема" width="218" />
  <img src="docs/screenshots/tests/02_chapter_question_dark.png" alt="Вопрос теста по главе" width="218" />
</p>

### Экзамены и прогресс
<p align="center">
  <img src="docs/screenshots/exams/01_exam_menu_light.png" alt="Выбор экзамена, пробные экзамены A–D" width="204" />
  <img src="docs/screenshots/exams/02_official_sample_a_eink.png" alt="Пробный экзамен A с таймером" width="204" />
  <img src="docs/screenshots/statistics/01_statistics_light.png" alt="Статистика обучения" width="204" />
  <img src="docs/screenshots/statistics/02_mistakes_review_light.png" alt="Тренировка ошибок" width="204" />
</p>

### Выбранный ответ в четырёх режимах
<p align="center">
  <img src="docs/screenshots/learning/03_answer_selected_light.png" alt="Выбранный ответ — светлая тема" width="204" />
  <img src="docs/screenshots/learning/04_answer_selected_dark.png" alt="Выбранный ответ — тёмная тема" width="204" />
  <img src="docs/screenshots/learning/05_answer_selected_reading.png" alt="Выбранный ответ — режим чтения" width="204" />
  <img src="docs/screenshots/learning/06_answer_selected_eink.png" alt="Выбранный ответ — E-Ink" width="204" />
</p>

<details>
<summary>Ещё скриншоты: меню, настройки, шрифты E-Ink и проверка ответа</summary>

#### Главные экраны — светлый / тёмный / чтение / E-Ink
<p align="center">
  <img src="docs/screenshots/home/01_home_light.png" alt="Главное меню — светлая тема" width="204" />
  <img src="docs/screenshots/home/02_home_dark.png" alt="Главное меню — тёмная тема" width="204" />
  <img src="docs/screenshots/home/03_home_reading.png" alt="Главное меню — режим чтения" width="204" />
  <img src="docs/screenshots/home/04_home_eink.png" alt="Главное меню — E-Ink" width="204" />
</p>

#### Настройки
<p align="center">
  <img src="docs/screenshots/settings/01_settings_light.png" alt="Настройки — светлая тема" width="204" />
  <img src="docs/screenshots/settings/02_settings_dark.png" alt="Настройки — тёмная тема" width="204" />
  <img src="docs/screenshots/settings/03_settings_reading.png" alt="Настройки — режим чтения" width="204" />
  <img src="docs/screenshots/settings/04_settings_eink.png" alt="Настройки — E-Ink" width="204" />
</p>

#### Дополнительные функции
<p align="center">
  <img src="docs/screenshots/tests/01_chapter_list_dark.png" alt="Список тестов по главам" width="204" />
  <img src="docs/screenshots/learning/07_answer_result_dark.png" alt="Результат проверки ответа — тёмная тема" width="204" />
  <img src="docs/screenshots/learning/08_eink_font_19sp.png" alt="E-Ink — размер текста 19 sp" width="204" />
  <img src="docs/screenshots/learning/09_eink_font_24sp.png" alt="E-Ink — размер текста 24 sp" width="204" />
</p>

</details>
<!-- ISTQB_GALLERY_END -->
