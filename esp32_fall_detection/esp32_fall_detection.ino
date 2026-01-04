
// Components :
//ESP32 
//Gyroscope (mpu6050 )
// LCD Screen 
// GPS (gps neo 6m) 
#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

// ==================== SETTINGS ====================
const char* ssid      = "_" // Wi-fi  name ;
const char* password  = "_"; // Wi-fi Password
const char* apiUrl    = "_"; // Ur Api to create new records



#define HEARTBEAT_PIN 34
#define SDA_PIN       21
#define SCL_PIN       22
#define MPU_ADDR      0x68

HardwareSerial GPSSerial(2);                    // UART2 for NEO-9M
TinyGPSPlus gps;

LiquidCrystal_I2C lcd(0x27, 16, 2);             

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 5*3600, 60000); 


float accelX, accelY, accelZ, gyroX, gyroY, gyroZ, accelMagnitude = 0;
int heartbeat = 0, lastValidHeartbeat = 0;
unsigned long lastFallTime = 0, lastSend = 0;
bool heartbeatOK = false, mpuOK = false, gpsOK = false;
double lat = 0, lng = 0;

// Heartbeat algorithm 
#define samp_siz 4
#define rise_threshold 5
float reads[samp_siz] = {0}, sum = 0, last = 0, before = 0;
float first = 600, second = 600, third = 600;
bool rising = false;
int rise_count = 0;
long last_beat = 0;
int ptr = 0;

bool wasUprightBefore = true;

void setup() {
  Serial.begin(115200);
  GPSSerial.begin(9600, SERIAL_8N1, 16, 17);    

  Wire.begin(SDA_PIN, SCL_PIN);
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0,0);
  lcd.print("ELDER SYSTEM");
  lcd.setCursor(0,1);
  lcd.print("Starting...");
  delay(1500);

  
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B); Wire.write(0x00);
  mpuOK = (Wire.endTransmission() == 0);        

  // WiFi + Time
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) delay(500);
  timeClient.begin();
  timeClient.update();

  lcd.clear();
  lcd.print("READY!");
  delay(1500);
  lcd.clear();

  last_beat = millis();
}

void loop() {
  timeClient.update();
  readGPS();
  if (mpuOK) readMPU6050();                     
  detectHeartbeat();

  // ONLY REAL FALLS → NO FALSE ALERTS
  if (mpuOK && detectRealFall() && millis() - lastFallTime > 15000) {  
    Serial.println("REAL FALL – SENDING ALERT + GPS!");
    sendFallAlert();
    lastFallTime = millis();
  }

  if (millis() - lastSend > 5000) {
    sendRealtimeHeartbeat();
    lastSend = millis();
  }

  updateLCD();
  delay(10);
}

// FALL DETECTION 
bool detectRealFall() {
  float gyroMag = sqrt(gyroX*gyroX + gyroY*gyroY + gyroZ*gyroZ);

  bool freeFall           = (accelMagnitude < 0.5);
  bool hardImpact         = (accelMagnitude > 3.5);
  bool wasUpright         = (accelZ > 0.75);
  bool nowLying           = (fabs(accelX) > 0.75 || fabs(accelY) > 0.75);
  bool orientationChanged = wasUprightBefore && nowLying;

  wasUprightBefore = wasUpright;

  bool fall = (hardImpact && orientationChanged && gyroMag > 180) ||
              (freeFall && hardImpact);

  return fall;
}


void readGPS() {
  while (GPSSerial.available() > 0) {
    if (gps.encode(GPSSerial.read())) {
      if (gps.location.isValid()) {
        lat = gps.location.lat();
        lng = gps.location.lng();
        gpsOK = true;
      }
    }
  }
}

void sendFallAlert() {
  if (WiFi.status() != WL_CONNECTED) return;

  String loc = gpsOK
    ? "https://maps.google.com/?q=" + String(lat,6) + "," + String(lng,6)
    : "No GPS fix";

  HTTPClient http;
  http.begin(String(apiUrl) + "?student_id=" + studentId);
  http.addHeader("Content-Type", "application/json");

  String json = "{"
    "\"student_id\":\"" + String(studentId) + "\","
    "\"title\":\"FALL DETECTED!\","
    "\"description\":\"EMERGENCY! BPM: " + String(lastValidHeartbeat) +
    " | Location: " + loc +
    " | Time: " + timeClient.getFormattedTime() + "\","
    "\"is_it_true\":true,"
    "\"integer_one\":" + String(lastValidHeartbeat) +
    "}";

  int code = http.POST(json);
  Serial.println(code > 0 ? "FALL + GPS SENT!" : "Send failed");
  http.end();

  lcd.clear();
  lcd.print("FALL ALERT SENT");
  lcd.setCursor(0,1);
  lcd.print("CHECK PHONE");
  delay(4000);
}

void updateLCD() {
  static unsigned long last = 0;
  if (millis() - last < 600) return;
  last = millis();
  lcd.clear();

  if (heartbeatOK && lastValidHeartbeat > 0) {
    lcd.print(lastValidHeartbeat); lcd.print(" BPM   ");
  } else {
    lcd.print("NO FINGER     ");
  }
  if (millis() - lastFallTime < 15000) {
    lcd.setCursor(11,0); lcd.print("FALL!");
  }

  String t = timeClient.getFormattedTime();
  t.remove(5);
  lcd.setCursor(0,1); lcd.print(t);
  lcd.setCursor(11,1);
  lcd.print(gpsOK ? "GPS" : "   ");
  lcd.print(heartbeatOK ? "H+" : "H-");
}

void detectHeartbeat() {
  long start = millis();
  float reader = 0; int samples = 0;
  while (millis() - start < 20) { reader += analogRead(HEARTBEAT_PIN); samples++; }
  reader /= samples;

  sum -= reads[ptr]; sum += reader; reads[ptr] = reader;
  last = sum / samp_siz; ptr = (ptr + 1) % samp_siz;

  if (last > before) {
    rise_count++;
    if (!rising && rise_count > rise_threshold) {
      rising = true;
      first = millis() - last_beat; last_beat = millis();
      float bpm_calc = 60000.0 / (0.4*first + 0.3*second + 0.3*third);
      heartbeat = (int)bpm_calc;
      if (heartbeat >= 30 && heartbeat <= 200) {
        lastValidHeartbeat = heartbeat;
        heartbeatOK = true;
      }
      third = second; second = first;
    }
  } else { rising = false; rise_count = 0; }
  before = last;
  if (millis() - last_beat > 8000) heartbeatOK = false;
}

void readMPU6050() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B); Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 14, true);
  if (Wire.available() >= 14) {
    accelX = (Wire.read()<<8|Wire.read())/16384.0;
    accelY = (Wire.read()<<8|Wire.read())/16384.0;
    accelZ = (Wire.read()<<8|Wire.read())/16384.0;
    Wire.read(); Wire.read();
    gyroX = (Wire.read()<<8|Wire.read())/131.0;
    gyroY = (Wire.read()<<8|Wire.read())/131.0;
    gyroZ = (Wire.read()<<8|Wire.read())/131.0;
    accelMagnitude = sqrt(accelX*accelX + accelY*accelY + accelZ*accelZ);
  }
}

void sendRealtimeHeartbeat() {
  if (WiFi.status() != WL_CONNECTED) return;
  HTTPClient http;
  http.begin(String(apiUrl) + "?student_id=" + studentId);
  http.addHeader("Content-Type", "application/json");
  String json = "{\"student_id\":\"" + String(studentId) +
                "\",\"title\":\"Heartbeat\",\"integer_one\":" + String(lastValidHeartbeat) + ",\"is_it_true\":false}";
  http.POST(json);
  http.end();
}
