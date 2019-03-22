# Elk M1 Binding

This binding supports an [Elk M1](https://www.elkproducts.com/product-catalog/m1-gold-cross-platform-control) alarm system. The connection is made through a network connection, so you must have an [ELK-M1XEP Ethernet Interface](https://www.elkproducts.com/products/elk-m1xep-m1-ethernet-interface) connected to your alarm system.

The current functionality allows:
* Up-to-the-moment status updates for alarm zones and areas.
* Ability to arm and disarm your alarm system
* Visibility into trouble status on your alarm system (power outage, missing keypad, etc.)

## Supported Things

This binding supports three different types of things currently:

* **bridge** - the main Elk M1 alarm system.
* **zone** - a zone represents windows and door sensors in your house.
* **area** - an area is a set of zones and keypads that can be individual controlled within the alarm system. This is mostly used for arming and disarming.

## Discovery

This binding will perform discovery on zones and areas by querying the main alarm panel for details. Note that discovery of the M1 Elk itself is not supported, you must configure the bridge and then the zones and areas can be discovered.

## Binding Configuration

This binding does not require special configuration.

## Thing Configuration

### Bridge

The bridge represents the main Elk M1 alarm panel. Connection parameters must be configured in order to connect to the ELK-M1XEP ethernet module. If you have the ELKRP software, these parameters should be easy to find. If your system was setup by a professional installer, you may need to get these parameters from your installer.

The bridge must be configured with an `ipAddress` and `port` for the network to connect to. Configuring a static IP address is recommended. In addition, if you have not selected the "Enable Non-Secure Port" option then you need to pass `useSSL` as true. Note that Non-Secure Port is not recommended, although it works fine. With the non-secure port there is absolutely no login security - anyone on the network can connect to that port and see and send messages to your alarm system. With SSL enabled, you must also configure the `username` and `password` parameters (not required for `useSSL` false).

Lastly, there is an optional `pincode` configuration parameter. This is the 4 (or 6, depending on your configuration) code that will be used when OpenHAB arms or disarms your system. If you do not configure the `pincode` then you can only monitor your system but cannot arm or disarm it from OpenHAB.

Example definition (from the thing file):

```
Thing elkm1:bridge:m1gold [ ipAddress="192.168.0.10", port="2601", pincode="1234", username="myusername", password="mypassword", useSSL="true" ]
```

### Zone

A zone represents a security zone within your alarm system - usually a window or a door or a group of windows/doors.

The zone must be configured with a `zoneId`, which is the zone number configured in the alarm panel.

Example definition (from the thing file):

```
Thing elkm1:zone:frontDoor "Front Door" (elkm1:bridge:m1gold) [ zoneId=3 ]
```

### Area

An area represents a security area, which contains a set of keypads and zones. (Note: keypads are not supported at this time in the binding)

The area must be configured with an `areaId`, which is the area number configured in the alarm panel.

Example definition (from the thing file):

```
Thing elkm1:area:house "House Area" (elkm1:bridge:m1gold) [ areaId=1 ]
```


## Channels

Here are the channels supported for the different things:

| Thing         | Channel                            | Item Type     | Description                                  |
|---------------|------------------------------------|---------------|--------------------------------------------- |
| area          | state                              | String        | Current state of area (1)                    |
| area          | armed                              | String        | Armed state. Update to arm or disarm area (2)|
| area          | armup                              | String        | Arm up state of area (3)                     |
| zone          | area                               | Number        | Area id that this zone belongs to            |
| zone          | config                             | String        | Physical configuration of zone (4)           |
| zone          | definition                         | String        | Type of zone (5)                             |
| zone          | status                             | String        | Current status of zone (6)                   |
| bridge        | general_trouble                    | Switch        | On if any trouble status is on               |
| bridge        | ac\_fail\_trouble                  | Switch        | AC (power) has failed, running on battery (7)|
| bridge        | box\_tamper\_trouble               | Switch        | Contact box (wireless) has been tampered with|                  | bridge        | box\_tamper\_zone                  | String        | Zone which has been tampered with            |
| bridge        | fail\_communicate\_trouble         | Switch        | Unable to communicate to the central station |
| bridge        | eeprom\_memory\_error\_trouble     | Switch        | EEProm memory has encountered an error       |
| bridge        | low\_battery\_control\_trouble     | Switch        | System backup battery problem                |
| bridge        | transmitter\_low\_battery\_trouble | Switch        | Wireless transmitter has low battery         |
| bridge        | transmitter\_low\_battery\_zone    | String        | Zone which contains low wireless battery     |
| bridge        | over\_current\_trouble             | Switch        | Over current condition detected              |
| bridge        | telephone\_fault\_trouble          | Switch        | Telephone reporting line fault               |
| bridge        | output\_2\_trouble                 | Switch        | Siren output (output 2) fault                |
| bridge        | missing\_keypad\_trouble           | Switch        | Keypad problem                               |
| bridge        | zone\_expander\_trouble            | Switch        | Zone expander problem                        |
| bridge        | output\_expander\_trouble          | Switch        | Output expander problem                      |
| bridge        | elkrp\_remote\_access_trouble      | Switch        | ELKRP remote access trouble                  |
| bridge        | common\_area\_not\_armed\_trouble  | Switch        | Common area not armed trouble                |
| bridge        | flash\_memory\_error\_trouble      | Switch        | Flash memory has encountered an error        |
| bridge        | security\_alert\_trouble           | Switch        | Security alert                               |
| bridge        | security\_alert\_zone              | String        | Zone which contains security alert           |
| bridge        | serial\_port\_expander\_trouble    | Switch        | Serial port expander problem                 |
| bridge        | lost\_transmitter\_trouble         | Switch        | Wireless transmitter is not communicating    |
| bridge        | lost\_transmitter\_zone            | String        | Zone which contains lost transmitter         |
| bridge        | ge\_smoke\_cleanme\_trouble        | Switch        | GE smoke detector optical port problem       |
| bridge        | ethernet\_trouble                  | Switch        | Ethernet expansion module problem            |
| bridge        | display\_message\_line1            | Switch        | Message is displayed in line 1               |
| bridge        | display\_message\_line2            | Switch        | Message is displayed in line 2               |
| bridge        | fire\_trouble                      | Switch        | Fire trouble                                 |
| bridge        | fire\_trouble\_zone                | Switch        | Zone which contains fire trouble             |    
   
(1) - State will be one of the following strings:
* **NoAlarmActive**
* **EntranceDelayIsActive**
* **AlarmAbortDelayActive**
* **FireAlarm**
* **MedicalAlarm**
* **PoliceAlarm**
* **BurglarAlarm**
* **Aux1Alarm**
* **Aux2Alarm**
* **Aux3Alarm**
* **Aux4Alarm**
* **CarbonMonoxideAlarm**
* **EmergencyAlarm**
* **FreezeAlarm**
* **GasAlarm**
* **HeatAlarm**
* **WaterAlarm**
* **FireSupervisory**
* **VerifyFire**

Alarm type depends on definition of violated zone (see zone "definition" channel)

(2) Armed will be one of the following strings:
* **Disarmed**
* **ArmedAway**
* **ArmedStay**
* **ArmedStayInstant**
* **ArmedToNight**
* **ArmedToNightInstant**
* **ArmedToVacation**

Note: This is the only channel that can be updated. Setting this to a new value will cause the system to arm or disarm this area using the pincode configured on the bridge.

(3) Armup will be one of the following strings:
* **NotReadyToArm**
* **ReadyToArm**
* **ReadyToArmButZoneIsViolated**
* **ArmedWithExitTimerWorking**
* **ArmedFully**
* **ForceArmedWithAForceArmZoneViolated**
* **ArmedWithABypass**
        
(4) - Config will be one of the following strings:
* **Open** - Normally open
* **EOL** - End of line supervised
* **Short** - Normally closed
* **Unconfigured**
* **Invalid** - Unexpected value returned

(5) - Definition will be one of the following strings:
* **Disabled**
* **BurglarEntryExit1**
* **BurglarEntryExit2**
* **BurglarPerimeterInstant**
* **BurglarInterior**
* **BurglarInteriorFollower**
* **BurglarInteriorNight**
* **BurglarInteriorNightDelay**
* **Burglar24Hour**
* **BurglarBoxTamper**
* **FireAlarm**
* **FireVerified**
* **FireSupervisory**
* **AuxAlarm1**
* **AuxAlarm2**
* **Keyfob**
* **NonAlarm**
* **CarbonMonoxide**
* **EmergencyAlarm**
* **FreezeAlarm**
* **GasAlarm**
* **HeatAlarm**
* **MedicalAlarm**
* **PoliceAlarm**
* **PoliceNoIndication**
* **WaterAlarm**
* **KeyMomentaryArmDisarm**
* **KeyMomentaryArmAway**
* **KeyMomentaryArmStay**
* **KeyMomentaryDisarm**
* **KeyOnOff**
* **MuteAudibles**
* **PowerSupervisory**
* **Temperature**
* **AnalogZone**
* **PhoneKey**
* **IntercomKey**

For more information on the operating characteristics of each type of zone, see the Elk M1 Installation Manual [link, login required](https://www.elkproducts.com/_literature_63668/ELK-M1G_Installation_Manual), page 30.

(6) - Status will be one of the following strings:
* **Normal** - Zone is in normal state (safe)
* **Violated** - Zone is in violated state (unsafe)
* **Trouble** - Problem detected in zone. Check connection to zone
* **Bypassed** - Zone is bypassed and will not alarm if violated
* **Invalid** - Unexpected value returned

(7) - Note that loss of AC power will not immediately alert, presumably to reduce false alarms in case of short power outages. In my testing, the status message is sent after a minute of power outage.

## Full Example

demo.things:

```
Thing elkm1:bridge:m1gold [ ipAddress="192.168.0.10", port="2601", pincode="1234", username="myusername", password="mypassword", useSSL="true" ]

Thing elkm1:area:house "House Area" (elkm1:bridge:m1gold) [ areaId=1 ]

Thing elkm1:zone:backDoor "Back Door" (elkm1:bridge:m1gold) [ zoneId=1 ]
Thing elkm1:zone:familyWindow "Family Room Window" (elkm1:bridge:m1gold) [ zoneId=2 ]
Thing elkm1:zone:frontDoor "Front Door" (elkm1:bridge:m1gold) [ zoneId=3 ]
```

demo.items:

```
String houseAlarmState { channel="elkm1:area:house:state" }
String houseAlarmArmed { channel="elkm1:area:house:armed" }
String houseAlarmArmUp { channel="elkm1:area:house:armup" }

String frontDoorStatus { channel="elkm1:zone:frontDoor:status" }
String backDoorStatus { channel="elkm1:zone:backDoor:status" }

Switch generalTrouble { channel="elkm1:bridge:m1gold:general_trouble" }
Switch elkAcFailureTrouble { channel="elkm1:bridge:m1gold:ac_fail_trouble" }
```

## Known limitations

* Polling has not yet been implemented, so the following things/channels are only set once during initial bridge initialization. As a work-around, to read these values again you can modify your things file (you can even just add a space somewhere unimportant):
    * zone:config
    * zone:definition
    * zone:area
    

## Notes

This binding requires the use of the Elk Ethernet Module. It is possible to extend this binding to use the RS232 (serial) protocol as the Elk messages are the same, but this is not currently implemented.