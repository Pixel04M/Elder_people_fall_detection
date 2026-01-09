# Elder People Fall Detection System

An IoT-based fall detection system designed to enhance the safety of elderly individuals.  
The system utilizes an ESP32 microcontroller to process sensor data and detect fall events in real-time. Upon detection, an immediate alert is sent to a companion Android mobile application for rapid response.

[![Download Android APK](https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android&logoColor=white)](app/release/Elder%20People%20Fall%20Detection%20.apk)

## Key Features

- Real-time fall detection using accelerometer and gyroscope sensor data
- Instant push notifications to the Android application
- Edge processing on ESP32 for low-latency response
- Designed specifically for elderly care and emergency monitoring

## Tech Stack

- **Microcontroller**: ESP32
- **Development Environment**: Arduino IDE
- **Sensors**: Accelerometer & Gyroscope (e.g., MPU6050)
- **Connectivity**: IoT (Wi-Fi/Bluetooth)
- **Mobile App**: Android (Native or Flutter/Kotlin)

## Source Code

ESP32 Fall Detection Implementation:  
[View on GitHub](https://github.com/Pixel04M/Elder_people_fall_detection/tree/974fdc39c159fa2fb4451e4f71429c06331e0984/esp32_fall_detection)

## Installation & Usage

1. **ESP32 Firmware**  
   - Clone the repository and open the project in Arduino IDE.  
   - Upload the code to your ESP32 board.

2. **Android App**  
   - Download the APK from the button above.  
   - Install on your Android device and configure the connection to the ESP32.

## Future Improvements

- Add machine learning for improved fall detection accuracy
- Integrate GPS location in alerts
- Support for multiple caregivers

---

Feel free to contribute or report issues!  
For any questions, open an issue on the repository.
