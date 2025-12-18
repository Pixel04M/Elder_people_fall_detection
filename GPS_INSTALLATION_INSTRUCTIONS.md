# GPS Library Installation Instructions

## Error Fix: TinyGPS++ Library Not Found

If you're getting the error: `'TinyGPS' does not name a type`, you need to install the TinyGPS++ library.

### Option 1: Install TinyGPS++ Library (Recommended if using GPS)

1. Open Arduino IDE
2. Go to **Tools** -> **Manage Libraries**
3. Search for **"TinyGPSPlus"**
4. Install **"TinyGPSPlus" by Mikal Hart** (Version 1.0.3 or later)
5. After installation, in `esp32_fall_detection.ino`:
   - Uncomment the line: `// #include <TinyGPS++.h>`
   - Change: `#define GPS_ENABLED 0` to `#define GPS_ENABLED 1`
6. Upload the code

### Option 2: Disable GPS (Use without GPS module)

If you don't have a GPS module or don't want to use it:

1. Keep `#define GPS_ENABLED 0` in the code
2. The code will compile and work without GPS
3. Location data will show as "Not available" in the app
4. All other features (fall detection, heartbeat) will work normally

### Current Status

The code is currently set to **GPS_ENABLED 0**, which means:
- ✅ Code will compile without TinyGPS++ library
- ✅ Fall detection will work
- ✅ Heartbeat monitoring will work
- ❌ GPS location will not be available

### To Enable GPS Later

1. Install TinyGPS++ library (see Option 1)
2. Uncomment: `#include <TinyGPS++.h>`
3. Change: `#define GPS_ENABLED 0` to `#define GPS_ENABLED 1`
4. Re-upload code

