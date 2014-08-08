// Included libraries
#include <Adafruit_CC3000.h>
#include <SPI.h>

// Debugging definition
#define SHOW_MESSAGES 1 // Set to 1 to print all messages to serial port, 0 to not print any (used for debugging)

// Pins
#define INDICATOR_LIGHT_PIN 1
#define ANGLE_PIN A1
#define PRESSURE_PIN A2

// WiFi definitions
#define WLAN_SSID "AndroidAP" // WiFi network name
#define WLAN_PASSWORD "abaqabaqabaq" // WiFi network password
#define WLAN_SECURITY WLAN_SEC_WPA2 // WiFi network security type
#define WIFI_PORT 7 // WiFi connection port number

// Other definitions for WiFi shield
#define ADAFRUIT_CC3000_IRQ 3 // Must be an interrupt pin
#define ADAFRUIT_CC3000_VBAT 5
#define ADAFRUIT_CC3000_CS 10

// Create WiFi shield object and server
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed
Adafruit_CC3000_Server chatServer(WIFI_PORT);

// Function to perform upon startup
void setup()
{
  // Open serial port
#ifdef SHOW_MESSAGES
  Serial.begin(115200);
  Serial.println(F("Opened serial port"));
#endif
  
  // Set pin modes for I/O
  pinMode(INDICATOR_LIGHT_PIN, OUTPUT);
  pinMode(ANGLE_PIN, INPUT_PULLUP);
  pinMode(PRESSURE_PIN, INPUT_PULLUP);
  
  // Turn off indicator light
  digitalWrite(INDICATOR_LIGHT_PIN, LOW);
  
  // Initialize WiFi module
  if(!cc3000.begin())
  {
#ifdef SHOW_MESSAGES
    Serial.println(F("Unable to initialize WiFi module."));
#endif
  }
  else
  {
#ifdef SHOW_MESSAGES
    Serial.println(F("Initialized WiFi module."));
#endif
  }
  
  // Connect to WiFi network
#ifdef SHOW_MESSAGES
  Serial.print(F("Attempting to connect to "));
  Serial.print(WLAN_SSID);
  Serial.print(F("... "));
#endif
  if(!cc3000.connectToAP(WLAN_SSID, WLAN_PASSWORD, WLAN_SECURITY))
  {
#ifdef SHOW_MESSAGES
    Serial.println(F("Failed"));
#endif
  }
  else
  {
#ifdef SHOW_MESSAGES
    Serial.println(F("Connected"));
#endif
  }
  
  // Get DHCP from WiFi
#ifdef SHOW_MESSAGES
  Serial.print(F("Requesting DHCP... "));
#endif
  while(!cc3000.checkDHCP())
  {
    // Wait and restart
    delay(50);
  }
  
#ifdef SHOW_MESSAGES
  Serial.println(F("Received"));
  uint32_t ipAddress, netmask, gateway, dhcpserv, dnsserv;
  while(!cc3000.getIPAddress(&ipAddress, &netmask, &gateway, &dhcpserv, &dnsserv))
  {
    Serial.println(F("Failed fetching network information, retrying... (takes a few tries)"));
  }
  
  Serial.print(F("\nIP Addr: ")); cc3000.printIPdotsRev(ipAddress);
  Serial.print(F("\nNetmask: ")); cc3000.printIPdotsRev(netmask);
  Serial.print(F("\nGateway: ")); cc3000.printIPdotsRev(gateway);
  Serial.print(F("\nDHCPsrv: ")); cc3000.printIPdotsRev(dhcpserv);
  Serial.print(F("\nDNSserv: ")); cc3000.printIPdotsRev(dnsserv);
  Serial.println();
  Serial.println(F(""));
#endif
  
  // Start listening for messages
  chatServer.begin();
  
  // Light indicator light
  digitalWrite(INDICATOR_LIGHT_PIN, HIGH);
}

// Main loop
void loop()
{
  // Create client object
  Adafruit_CC3000_ClientRef client = chatServer.available();
  
  // Read current values from sensors
  int angleValue = analogRead(ANGLE_PIN) / 4;
  int pressureValue = analogRead(PRESSURE_PIN) / 4;
  
  // Print what values were read
#ifdef SHOW_MESSAGES
  Serial.print(F("Angle: ")); Serial.println(angleValue);
  Serial.print(F("Pressure: ")); Serial.println(pressureValue);
#endif

  // Write current values to host
  chatServer.write(angleValue);
  chatServer.write(pressureValue);
  
  // Wait for host response before continuing
  int cmd = 0;
  
  while(1)
  {
    client = chatServer.available();
    
    if(client && client.available() > 0)
    {
      while ((cmd = client.read()) == -1);
      if(cmd != 13 && cmd != 10)
      {
        break;
      }
    }
  }
  
  // Print what byte was receieved
#ifdef SHOW_MESSAGES
  Serial.print(F("Received byte: ")); Serial.println(cmd);
#endif
}
