# Weather App 🌦️

A modern, beautiful Android weather application built with Kotlin and MVVM architecture.

## Features ✨

*   **Real-time Weather**: Get current weather conditions for any city.
*   **Hourly Forecast**: View detailed 3-hour interval weather data for the current day and future days.
*   **5-Day Forecast**: Plan ahead with a detailed 5-day weather forecast.
*   **Interactive Day Selection**: Click on any day in the 5-day forecast to see its specific hourly breakdown.
*   **Smart Search**: Search for cities with auto-suggestions and error handling.
*   **Dynamic Backgrounds**: The app interface changes based on the weather (Sunny, Rainy, Snowy, etc.).
*   **Visual Feedback**: Selected day is highlighted with a stylish border for better UX.
*   **Multi-Language Support**: Seamlessly switch between English (EN) and Turkish (TR) without restarting the app.
*   **Location-Based**: Automatically detects your location to show local weather (Prioritizes Province/State).

## Tech Stack 🛠️

*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Network**: Retrofit & Gson
*   **Concurrency**: Coroutines & Flow
*   **Image Loading**: Coil
*   **UI**: XML Layouts, ConstraintLayout, RecyclerView
*   **Location**: Google Play Services Location

## Setup & Installation 🚀

1.  Clone the repository:
    ```bash
    git clone https://github.com/yusufsenturk0/Weather-App.git
    ```
2.  Open the project in **Android Studio**.
3.  **Important**: You need an OpenWeatherMap API Key.
    *   Create a file named `local.properties` in the root directory (if it doesn't exist).
    *   Add your API key like this:
        ```properties
        API_KEY=your_api_key_here
        ```
4.  Build and run the app!


