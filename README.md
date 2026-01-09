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
- **Sensors**: Heartbeat & Gyroscope & NEO-6M
- **Connectivity**: IoT (Wi-Fi)
- **Mobile App**: Android (Kotlin)

## Screenshots / Showcase


<table>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/Pixel04M/Elder_people_fall_detection/master/photo_2026-01-09_15-29-47.jpg" width="320" alt="Device overview"><br><sub>Notification</sub></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Pixel04M/Elder_people_fall_detection/master/photo_2026-01-09_15-30-09.jpg" width="320" alt="Sensor connection"><br><sub>Fall Alart Details</sub></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Pixel04M/Elder_people_fall_detection/master/photo_2026-01-09_15-30-23.jpg" width="320" alt="Wearable placement"><br><sub>Records</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/Pixel04M/Elder_people_fall_detection/master/photo_2026-01-09_15-30-23%20(2).jpg" width="320" alt="Side view"><br><sub>Filter Only Alarts/ All Records </sub></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Pixel04M/Elder_people_fall_detection/master/photo_2026-01-09_15-30-31.jpg" width="320" alt="Full setup"><br><sub>Realtime Heartbeat Monitoring</sub></td>
    <td align="center"></td> <!-- empty cell for balance if you want only 5 images -->
  </tr>
</table>


## Code for ESP
ESP32 Fall Detection Implementation:  
[View on GitHub](https://github.com/Pixel04M/Elder_people_fall_detection/tree/974fdc39c159fa2fb4451e4f71429c06331e0984/esp32_fall_detection)

ESP Connections :
[Repo](https://github.com/Pixel04M/esp32-conections-.git)

## Installation & Usage
1. **ESP32 Firmware**
   - Clone the repository and open the project in Arduino IDE.
   - Upload the code to your ESP32 board.

2. **Android App**
   - Download the APK from the button above.
   - Install on your Android device and configure the connection to the ESP32.

## Future Improvements
- Add machine learning for improved fall detection accuracy
- Support for multiple caregivers

---
Feel free to contribute or report issues!  
For any questions, open an issue on the repository.
