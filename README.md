# Y Calculator 🧮

**A lightweight scientific calculator for Android, inspired by the classic Casio fx-82MS.**

Y Calculator aims to bring the familiar experience of a physical scientific calculator to your smartphone, combining a classic keypad layout with a clean LCD-style display and a minimal, responsive interface.

Designed with simplicity, speed, and usability in mind.

## ✨ Features

- 🧮 **Scientific calculations** — Basic arithmetic and supported scientific functions.
- 📐 **Trigonometry** — Sine, cosine, tangent, and supported inverse functions.
- 🔢 **Powers and roots** — Squares, exponents, square roots, and related operations.
- 📊 **Logarithms** — Common and natural logarithmic functions.
- 🔄 **Angle modes** — Degree and radian support.
- ⌨️ **Physical calculator-inspired keypad** — Familiar key arrangement and secondary functions.
- 🖥️ **LCD-style display** — A simple, high-contrast display with an expression line, result, cursor, and status indicators.
- ⚡ **Lightweight experience** — Focused on responsive interactions and efficient resource usage.
- 📱 **Android 11+** — Designed for Android devices running Android 11 or later.
- 📴 **Offline functionality** — Calculator operations do not require an internet connection.
- 🎯 **Minimal UI** — No unnecessary dashboards, feeds, or distracting interface elements.

> Feature availability depends on the functions implemented in the current version.

## 🎨 Design Philosophy

Y Calculator takes inspiration from the classic Casio fx-82MS scientific calculator.

The goal is to preserve the familiarity of a physical calculator while adapting it to a smartphone.

- A classic scientific keypad layout.
- A clean, readable LCD-style display.
- Carefully aligned primary and secondary key labels.
- Minimal visual effects.
- Responsive touch interactions.
- A compact interface that prioritizes the calculator itself.

The focus is on functionality rather than unnecessary visual complexity.

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Minimum Android version | Android 11 (API 30), if configured as the minimum SDK |
| Programming language | Kotlin |
| UI framework | Jetpack Compose or the framework used in the project |
| Build system | Gradle |
| Connectivity | Offline-first |

*Update this table to match the actual project configuration before publishing.*

## 📱 Compatibility

- **Minimum Android version:** Android 11, provided API 30 is configured as `minSdk`.
- **Target devices:** Android smartphones with different screen sizes and display densities.
- **Connectivity:** No internet connection required for calculator operations.
- **Orientation:** Portrait-oriented calculator interface.

The project aims to support resource-constrained devices, including phones with less than 3 GB of RAM. Actual performance depends on the device and should be verified through testing.

## 🚀 Getting Started

### Install the APK

If a compiled APK is available:

1. Download the APK from the project's GitHub Releases page.
2. Open the downloaded file on your Android phone.
3. Allow installation from that source if Android requests permission.
4. Install and launch **Y Calculator**.

Only install APKs from sources you trust.

### Build from source

**Requirements**

- Android Studio or a compatible Android development environment.
- A compatible JDK.
- Android SDK with the project's required platform and build tools.
- Git.

Clone the repository:

```bash
git clone https://github.com/YOUR-USERNAME/Y-Calculator.git
cd Y-Calculator
```

Open the project in Android Studio and allow Gradle to synchronize.

Build a debug APK:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

The APK is typically generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The exact output location may vary depending on the project's Gradle configuration.

## 🧪 Testing

Before releasing a version, verify:

- Basic arithmetic and operator precedence.
- Parentheses and negative numbers.
- Trigonometric functions in degree and radian modes.
- Powers, roots, and logarithms.
- Scientific notation and error handling.
- SHIFT and ALPHA functions.
- Memory and Ans behavior, where implemented.
- Cursor movement and expression editing.
- Rapid consecutive key presses.
- Key-label alignment on different screen sizes.
- Performance on lower-memory devices.

## 🗺️ Roadmap

Potential improvements for future versions:

- [ ] Refine responsive keypad layouts.
- [ ] Improve cursor positioning and LCD display alignment.
- [ ] Optimize input responsiveness.
- [ ] Expand automated calculation tests.
- [ ] Validate behavior against reference scientific calculations.
- [ ] Test across different Android devices.
- [ ] Publish versioned APKs through GitHub Releases.

## 📦 Releases

Visit the repository's **Releases** section to find published APKs:

`https://github.com/yedhushnav-suresh/Y-calculator`


## 🤝 Contributing

Contributions, bug reports, and suggestions are welcome.

If you find a bug:

1. Check whether it has already been reported.
2. Open a GitHub issue with a clear description.
3. Include your Android version and device details.
4. Provide steps to reproduce the problem.

For code contributions, create a branch, make your changes, test them, and submit a pull request.

## 📄 License

Choose and add a license before distributing the project publicly.

If you want others to use, modify, and distribute the source code, consider a permissive license such as the MIT License. Add a `LICENSE` file containing the actual license terms before claiming the project is MIT-licensed.

## ⚠️ Disclaimer

Y Calculator is an independently developed application inspired by the layout of a classic scientific calculator. It is not an official Casio product and is not affiliated with or endorsed by Casio Computer Co., Ltd.

Product names and trademarks belong to their respective owners.

---

**Y Calculator — Just calculate. Nothing extra.** 🧮
