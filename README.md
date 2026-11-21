# Typo

Typo is an Android companion app for controlling a computer's keyboard remotely over a local network. It lets you type text from your Android device and send keyboard actions such as **Backspace** and **Enter** to the paired computer.

The Android app is designed to work with the desktop-side Typo receiver.

## Features

- Send text from an Android device to a paired computer.
- Discover a compatible receiver on the local network.
- Save a receiver's IP address for later connections.
- Send Backspace and Enter actions.
- Check network connectivity and refresh the connection state.

## How it works

1. Run the Typo desktop receiver on the computer.
2. Connect the Android phone and computer to the same local network.
3. Open Typo on Android and use the discovery option, or enter the computer's IP address manually.
4. Save the address and send text or keyboard actions.

The Android app sends HTTP POST requests to the receiver's `/send_message` endpoint. The receiver port and shared authentication value are read from the Android app's local `assets/env` configuration.

## Related project

- [Typo desktop companion](https://github.com/masterweb801/Typo-Driver)

## Build from source

### Requirements

- Android Studio
- JDK 11-compatible build environment
- Android SDK configured for the project

### Build

Clone the repository:

```bash
git clone https://github.com/masterweb801/Typo.git
cd Typo
```

Open the project in Android Studio and allow Gradle to sync. Configure the expected local `assets/env` file with the receiver's `PORT` and `SECRET` values before running the app. Do not commit real secrets or personal deployment configuration.

You can also build a release APK from the project root:

```bash
./gradlew assembleRelease
```

On Windows, use:

```bat
gradlew.bat assembleRelease
```

The generated APK is normally located under `app/build/outputs/apk/release/`.

## Current networking note

The current repository version communicates with the desktop receiver over plain HTTP on the local network. The shared authentication value is sent in the request body, so it is not encrypted in transit. Use Typo only on a trusted network and do not expose the receiver port to the public internet.

A future version is planned to introduce encrypted networking. Until that work is implemented and reviewed, do not assume the current release provides end-to-end encryption.

## Technology

- Kotlin
- Android SDK
- OkHttp
- Kotlin Coroutines
- Gradle Kotlin DSL

## Project

Developed by **Logic Realm**.

- [Logic Realm](https://logicrealm.rf.gd/)
- [Typo source code](https://github.com/masterweb801/Typo)
