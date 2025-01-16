# KeepUp - Fitness Tracking Application

**KeepUp** is a fitness tracking application developed in Kotlin using Jetpack Compose for Android. The main purpose of the app is to help users track different types of activities, such as running, cycling, and walking. The application utilizes Google Maps for location visualization, the phone's GPS to track real-time position, and other sensors to enhance the fitness tracking experience.

## Features

- **Real-time Location Tracking**: The app uses the phone's GPS to provide accurate real-time location tracking.
- **Activity Types**: Users can choose between different activity types: running, cycling, walking, and more.
- **Distance Measurement**: Tracks the total distance covered during the activity using the GPS data.
- **Elapsed Time Tracking**: Shows the elapsed time during the activity.
- **Google Maps Integration**: Displays the user's route on a Google Map within the app.
- **Compass Feature**: Provides a visual compass on the screen to help users orient themselves during their activity.
- **Start/Stop Functionality**: Start and stop activity tracking, with all data being updated in real-time.
- **User-Friendly Interface**: Built using Jetpack Compose for a modern and intuitive UI.

## Screenshots

*Insert relevant screenshots of the app here*

## Requirements

- **Android SDK**: API level 26 or higher.
- **Google Maps API Key**: Required for the map functionality.
- **Internet Access**: Needed for accessing Google Maps services.

## Getting Started

1. **Clone the repository**:
   ```sh
   git clone https://github.com/Fabian12Florin/keepup-fitness-app.git
   ```

2. **Open the Project** in Android Studio.

3. **Configure the Google Maps API Key**:
   - Go to the `local.properties` file or directly in the manifest, add your Google Maps API key.

4. **Run the Application** on an Android device or emulator.

## Permissions

The application requires the following permissions:

- **Location Permission**: To track the user's real-time location during activities.
- **Sensor Permission**: To access device sensors like the compass for orientation.

Make sure to grant these permissions when prompted during the app usage.

## How to Use

1. **Login/Register**: Start by logging in or creating an account.
2. **Select Activity**: Choose the type of activity you want to track (e.g., running, cycling).
3. **Start Tracking**: Press the "Start" button to begin tracking your activity. The app will show your location on the map, distance covered, elapsed time, and other statistics.
4. **Stop Tracking**: Press "Stop" to end the session and save the activity data.

## Technologies Used

- **Kotlin**: Programming language used for app development.
- **Jetpack Compose**: UI toolkit for building the user interface.
- **Google Maps API**: Used for map and location services.
- **Sensors**: Used to gather orientation data for the compass feature.

## Contribution

Contributions are welcome! If you'd like to improve the project, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -m 'Add new feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.

## Contact

For any questions or suggestions, feel free to reach out:

- **Email**: [ffabian08642@gmail.com]
- **GitHub**: [https://github.com/Fabian12Florin]

Thank you for checking out **KeepUp**! We hope it helps you stay motivated and active.

