<div align="center">

# 💸 Split

**A simple, clean tip calculator for Android — calculate the tip, see the total, and split the bill.**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)

</div>

---

## ✨ Features

- 🧾 **Bill amount input** with validation (digits and up to 2 decimal places)
- ⚡ **Quick tip presets**: 10%, 12%, 15%, 18%, or enter a custom percentage
- 👥 **Split the bill** between 1–50 people and see the amount per person
- 🔼 **Round up** the tip to the next whole shekel
- 💳 **Total amount** (bill + tip) shown at a glance
- ₪ **Israeli currency formatting** (he-IL locale)
- 🔄 **Keeps your input** when the screen rotates
- 🎨 **Material 3 design** that follows the app theme

## 📱 Screenshots

| Main screen | Split between people |
|:---:|:---:|
| <img src="screenshots/main.png" width="250"/> | <img src="screenshots/split.png" width="250"/> |

> Add your screenshots to a `screenshots/` folder in the repository root.

## 🛠️ Tech Stack

| | |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Design system** | Material 3 |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |
| **IDE** | Android Studio |

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/Split.git
   ```
2. **Open** the project in Android Studio.
3. **Sync** Gradle and let the dependencies download.
4. **Run** the app on an emulator or a physical device (Android 7.0+).

## 📂 Project Structure

```
app/src/main/
├── java/com/dongeria/split/
│   ├── MainActivity.kt      # UI and tip calculation logic
│   └── ui/theme/            # Colors, typography and theme
└── res/
    ├── drawable/            # Icons (money, percent)
    └── values/strings.xml   # App strings
```

## 🧮 How It Works

```kotlin
tip       = bill × (tipPercent / 100)    // rounded up if "Round up" is on
total     = bill + tip
perPerson = total / people
```

The calculation is a pure function (`calculateTip`) that returns a `TipResult`, which keeps it easy to unit test.

## 🗺️ Roadmap

- [ ] Hebrew translation with RTL support
- [ ] Unit tests for `calculateTip`
- [ ] Dark mode screenshots
- [ ] Save the last used tip percentage

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

