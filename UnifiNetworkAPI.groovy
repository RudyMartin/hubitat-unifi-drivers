/*
* UnifiNetworkAPI
*
* Description:
* This Hubitat driver allows polling of the Unifi API, initially geared around a Unifi Dream Machine Pro but supports many controllers.
*
* Overall Setup:
* 1) Add the UnifiNetworkAPI.groovy and UnifiNetworkChild drivers (whichever ones you have devices for) as a new user driver
* 2) Add a Virtual Device to be the parent
* 3) Enter the Unifi's IP/Hostname, Username, Password, and select the Controller Type in the Preference fields and Save Preferences
* REQUIRED that the Unifi user has local access to the controller and is not using MFA. If needed, create a new admin user locally on the
*   controller and select the "Restrict to local access only" checkbox.
* REQUIRED for "Other Unifi Controllers" Controller Type: Set the Controller Port #. This Preference will appear after you Save Preferences.
*   Set it, then Save Preferences again. This defaults to 8443 but newer version software may be using 443.
* OPTIONAL: Override the Site Name (if you want something other than the main Default site of the controller auto-populated), Refresh Rate,
*   Logging
* OPTIONAL: If you want to use the network presence detection capability, you must also load the UnifiNetworkChild-Presence.groovy driver
*   and add MAC addresses to use for Presence detection as desired in the Preference
* OPTIONAL: If you want to enable the Show Unifi Devices as Children Preference, you need to load additional UnifiNetworkChild drivers
*   as appropriate for the devices you expect. If a Unifi device has not been identified by the parent driver yet it will use the
*   UnifiNetworkChild.groovy driver with limited attributes and commands. You can switch to a more appropriate driver within the child
*   device's settings (and rename the Generic portion of the DNI to the type of driver you use ex: USW24PoE if using the
*   UnifiNetworkChild-USW24Poe.groovy driver)
*   If the Show Unifi Devices... preference is disabled it will automatically delete all child devices EXCEPT Presence ones (which are not
*   related to this feature. Enabling it again will allow them to be automatically recreated.
*
* Features List:
* Allows a Ping to be performed to check a device IP via the Hubitat (requires hub version 2.2.7)
* Auto-populates the Default site of the unifi controller but allows overiding that if needed
* Shows a list of alarms on the controller that have not been archived. This can get VERY long if you have many events.
* Ability to create child devices based on MAC addresses being monitored for presence and check those every minute
* Checks the controller's site so you know the health of wlan, wan, www, lan, and vpn
* Shows the controller's CPU and memory usage as well as uptime
* Sets child device label to the name reported by the controller for that specific MAC
* Ability to check if a MAC address exists in the controller records
* Ability to check if a MAC address is currently online (based on controller determination of online)
* Ability to block a MAC address from a Unifi AP (does not really work if it is connected to a NON Unifi access point)
* Ability to unblock a MAC address from a Unifi AP (does not really work if it is connected to a NON Unifi access point)
* Ability to archive all alarms currently active
* Ability to reboot and power down the controller
* Ability to power cycle a PoE port
* Ability to specifically handle the following Ubiquiti devices:
  * Unifi Dream Machine Pro SE (UDMP SE)
  * Unifi Dream Machine Pro (UDMP)
  * Unifi Dream Machine (UDM)
  * Unifi Dream Router (UDR)
  * Unifi Express Gateway (UX)
  * Unifi Gateway Lite & Max (UXG & UXGB)
  * Security Gateway Pro (UGW4) = USG4Pro
  * Security Gateway (UGW3) = USG3
  * Security Gateway (USG)
  * Cloud Gateway Max (UCGMAX)
  * Unifi Access Points with status LED ring (AP)
    * BZ2LR = Unifi AC-LR
    * UALR6v2 = Unifi WiFi 6 LR
    * U7NHD = Unifi Access Point nanoHD
    * UAP6MP = Unifi Access Point 6 Pro
    * UAL6 = U6-Lite
    * U7P = UAP-Pro
    * U7MP = UAP-AC-M-Pro
    * U7PG2 = UAP-AC-Pro
    * U7LT = UAP-AC-Lite
    * U7HD = UAP-AC-HD
    * UCXG = UAP-XG
    * U6M = Unifi U6 Mesh
    * UAPL6 = U6+
    * U7PRO = U7 Pro
  * Unifi Access Points without status LED ring (BasicAP)
    * U7MSH = UAP-AC-M
	* U7PIW = U7 Pro In Wall
  * Unifi HD In Wall APs
    * UHDIW = UAP-IW-HD-US
    * U6IW = U6-IW-US 
    * U6ENT = U6-Enterprise-IW-US
  * Unifi AC In Wall AP
    * U7IW = AC-IW
  * Unifi Redundant Power System (RPS)
  * Unifi Power Switch (UP6)
  * Unifi Power Plug (UP1)
  * Unifi SmartPower Pro PDU (USPPDUP)
  * Unifi Switch Mini
    * Unifi Switch Mini (USMINI)
    * Unifi Switch Flex Mini 2.5G (USWED35)
  * Unifi Switch Flex (US5FP)
  * Unifi 48 Port PoE Switches (USW48PoE)
    * US48PRO = USW-Pro-48-PoE
  * Unifi 48 Port non-PoE Switches (USW48)
    * US48 = US-48-G1
  * Unifi 48 Port Pro Max non-PoE Switch (USPM48)
  * Unifi 48 Port Pro Max PoE Switch (USPM48P)
  * Unifi 24 Port Pro Max non-PoE Switch (USPM24)
  * Unifi 24 Port Pro Max PoE Switch (USPM24P)
  * Unifi 16 Port Pro Max PoE Switch (USPM16P)
  * Unifi 16 Port Pro Max non-PoE Switch (USPM16)
  * Unifi 24 Port PoE Switches (USW24PoE)
    * USL24P250 = US-24-250
    * US24P250 = US-24-250
    * USL24P = USW-24-PoE
    * US624P = 24 Port Enterprise PoE Switch
  * Unifi 24 Port non-PoE Switches (USW24)
    * US24 = US-24-G1
    * USL24 = USW-24-G2
  * Unifi 16 Port PoE Switches (USW16PoE)
    * US16P150 = US-16-150
  * Unifi 16 Port Lite PoE Switches (USW16LPoE)
    * USL16P or USL16LP = USW-Lite-16-POE
  * Unifi 16 Port non-PoE Switches (USW16)
  * Unifi 8 Port PoE Switches (USW8PoE)
    * US8P150 = US-8-150
	* USWED37 = USW-Flex-2.5G-8-PoE
  * Unifi 8 Port Lite PoE Switches (USW8LPoE)
	* USL8LPB = USW Lite 8 POE
    * USL8LP = USW-Lite-8-PoE
    * US8P60 = US-8-60W
  * Unifi 8 Port Switch with 1 PoE Passthrough (US8 & USC8)
  * Unifi 8 Port non-PoE Switches (USW8)
  * Unifi Aggregate Pro (USAGGPRO)
* Checks drdsnell.com for an updated driver on a daily basis
*
* Licensing:
* Copyright 2025 David Snell
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* Known Issue(s):
* 
* Version Control:
* 0.4.74 - Added recognition for USWED37 (Unifi Flex 2.5G PoE 8 Port Switch) and some additional data handling
* 0.4.73 - Corrected typo for USL8LPB
* 0.4.72 - Added model recognition for a new variant of the USW Lite 8 PoE switch as well as entirely new devices for the UCGMAX and U7PIW
*  and consolidated ChildType code from Event & State functions
* 0.4.71 - Changed the Flex Mini 2.5G to use the USMINI child driver
* 0.4.70 - Addition of Switch Flex Mini 2.5G detection, directing it to the US5FP child driver
* 0.4.69 - Correction to BlockMAC and UnblockMAC functions when handling multiple MACs as well as additional data handling
* 0.4.68 - Added support for 16 port Pro Max switches
* 0.4.67 - Removed rounding of days, hours, and minutes for Uptime and related attributes
* 0.4.66 - Zero out uptime when Presence devices become not present
* 0.4.65 - Tweaks to DriverStatus messaging and clearing Alarm data if Preference is disabled also supporting changed login Token name in
*  newer versions of Unifi OS
* 0.4.64 - Altering timings for scheduled events and handled some new data reported by API
* 0.4.63 - "Promote" Port maps to be Events so their information is accessible in Rules and update to "SetPortState" function
* 0.4.62 - Recognition of UXG Max and trigger additional defaults for UXG Max child device on generation
* 0.4.61 - Recognition for UGW3 to use the UGW child driver
* 0.4.60 - Recognition for AC-IW
* 0.4.59 - Last_WAN_IP, IP, and DeviceVersion now sent to child devices as Events and added RestartDevice function
* 0.4.58 - Added recognition for UXG and removal of "Driver Status"
* 0.4.57 - Correction to ProcessEvent function and removal of old driver-specific attributes when Preferences are saved
* 0.4.56 - Change to SendCommand to make it more specific of a type
* 0.4.55 - Added LCMBrightness function and handling for Etherlighting-related commands from Pro Max switches
* 0.4.54 - Added recognition for U7 Pro, Pro Max 24/48 PoE, and Pro Max 24/48 non-PoE switches and deleting old driver attributes
* 0.4.53 - Recognition added for the Unifi Express Gateway
* 0.4.52 - Added a command to rename a Presence child and change the old MAC in the Presence checking list as well
* 0.4.51 - Expanded block and unblock on parent device to allow for comma-separated lists of MACs, also changed following device attribute names:
*  "Driver Name" to "DriverName", "Driver Version" to "DriverVersion", "Driver Status" to "DriverStatus", "Manual Presence Result" to "Manual_Presence_Result",
*  "MAC Exists Result" to "MAC_Exists_Result", "Total Clients" to "Total_Clients", "Online Clients" to "Online_Clients", "Unifi Devices" to "Unifi_Devices",
*  "Last Login" to "Last_Login", "Last Refresh" to "Last_Refresh", "Last Presence Check" to "Last_Presence_Check", "Last Seen" to "LastSeen",
*  "Connected To MAC" to "ConnectedToMAC", "Connected To Name" to "ConnectedToName", and "Signal Strength" to "SignalStrength" to fix visual error on device page(s)
* 0.4.50 - Added Uptime information for Presence and ClientCheck features
* 0.4.49 - Change to receiving CSRF to account for changes in Unifi OS 3.2.8 
* 0.4.48 - Additional data handling
* 0.4.47 - Fix for Presence and CheckClient children not remaining if the "Show Unifi Devices as Children" Preference is disabled
* 0.4.46 - Added recognition of U6+ as a general AP
* 0.4.45 - Added support for Aggregate Pro switch
* 0.4.44 - Add recognition of Unifi Dream Router (UDR) and added descriptions to Controller Type and Site Override preferences
* 0.4.43 - Further change to SetPortState overrides handling, additional overall data handling
* 0.4.42 - Handling for additional Port Override parameters
* 0.4.41 - New method of handling Port and Outlet Overrides, reduced data handled from SetPortState due to limited data returned from API, and additional data handling
* 0.4.40 - Added an additional recognition for 24 port PoE switch, correction to power reporting so it is included in correct child device, and additional data handling
* 0.4.39 - Added some additional data handling including PoE total power reporting
* 0.4.38 - Added ability to force isChanged for child events
* 0.4.37 - Added support for U6ENT and U6IW, tweaking some PowerCyclePort-related logging
* 0.4.36 - Changed Port for PowerCycling to not be in quotes
* 0.4.35 - Added one new data point being returned
* 0.4.34 - Handle some new data coming back for UDMPs
* 0.4.33 - Added recognition of US-8/USC8 and USG (Security Gateway) and switched Uptime for children to an attribute
* 0.4.32 - Added recognition of USGPro4 (UGW4) and a minor change to allow forcing an Event "changed" notice
* 0.4.31 - Added recognition of UAP-AC-M-Pro as an access point
* 0.4.30 - Changes related to USP-PDU-Pro outlet & USB port handling as well as power reporting
* 0.4.29 - Cookie expiration may have changed so Login now happens every 10 minutes to account for it
* 0.4.28 - Added some error checking for null child devices
* 0.4.27 - Support for custom port number when using Other Unifi Controllers
* 0.4.26 - Correction for CheckClient failure
* 0.4.25 - Extra logging for CheckClient capability
* 0.4.24 - Added new CheckClient capabilities meant for checking up to 20 clients on an hourly basis
* 0.4.23 - Added model recognition for USL8LP, breaking out the older 8 Port PoE 60W switch
* 0.4.22 - Changes to port_table handling for existing data
* 0.4.21 - Added support for UDMP SE
* 0.4.20 - Update to driver update checking, additional presence device features, support for U6 Mesh devices
* 0.4.19 - Correction to include port configuration ID for port_overrides
* 0.4.18 - Correction to port_override handling
* 0.4.17 - Change to support millisecond-based Epoch
* 0.4.16 - Added units for general_temperature reporting
* 0.4.15 - Removed redundant CurrentStats check when saving Preferences and added additional model for 16 Port Lite PoE switch
* 0.4.14 - Correction to convert general device temperature (was already handled for CPU and such) and changes to port status handling
* 0.4.13 - Minor changes to better support device refreshing
* 0.4.12 - Added ability to get Unifi Device data for a specific Unifi device
* 0.4.11 - Correction for SmartPower Pro PDU USB port handling
* 0.4.10 - Added model recognition for the SmartPower Pro PDU
* 0.4.9 - Added model recognition for 2 additional Access Points, the 6 Pro and the nanoHD
* 0.4.8 - Presence device label only set from controller data if the label is null to begin with
* 0.4.7 - Added handling for RPS port on some switches, recognition of 24 port Enterprise switch, some additional data field handling,
*  unidentified Unifi devices are now added, but will have the placeholder UnifiNetworkChild driver with no real attributes or commands
* 0.4.6 - Started handling data for PoE switch ports and added leading 0s to port numbers <10 to help ordering
* 0.4.5 - Added support for a slew of network switches (8, 16, 24, and 48 ports both PoE and non-PoE varieties) and a handful of APs
*  as well as a number of new checks for null data being returned
* 0.4.4 - Added support for BasicAP child driver to handle APs without status LED rings (like the UAP-AC-M), UAP-Pro identified, correction
*  to WAN2 data check
* 0.4.3 - Moved child data processing to parent due to redundancy of the code, added support for Power Strip (UP6), In Wall AP (UHDIW),
*  NonPOESwitch (Mini - USMINI), & POESwitch (Flex - US5FP), rework of PowerCyclePort for switches
* 0.4.2 - Basic identification and support for Unifi Smart Plug and change to device identification for children
* 0.4.1 - Correct handling of unidentified switches (dump their data to Debug logging)
* 0.4.0 - Separating out presence child devices (always) and adding unifi devices in as separate children if UnifiChildren is enabled
* 0.3.3 - Correction to reboot and powerdown commands
* 0.3.2 - Made it so devices with regular presence checks will also be updated if a manual check is performed for that MAC,
*  added Advanced Commands that allow for power cycling ports, rebooting the controller, and powering down the controller
* 0.3.1 - Correction for driver update checking
* 0.3.0 - Changed to make this Network API specific with renaming various aspects
* 0.2.11 - Changed generating params to be section specific
* 0.2.10 - Added ability to Ping something from the driver using the Hubitat's Ping command (added in 2.2.7), error check for lack of CPU data,
*  ArchiveAlarms now immediately clears out the Alarms event on a successful response from the controller rather than waiting for the next status
* 0.2.9 - Added a preference for showing Alarms data (defaults to true), changed the 5 second delay before refresh and presence checks
* 0.2.8 - Adding a 5 second delay before the refresh and presence check reschedules on a successful login
* 0.2.7 - Reschedule 1 minute refresh and presence checks after a successful login attempt, made it so not-present devices
*  will result in the "Last Presence Check" value being updated also, added blocking and unblocking devices from Unifi APs,
*  added command to archive all alarms
* 0.2.6 - Changed way Unifi Devices is determined, it now adds "num_adopted" unifi devices
* 0.2.5 - Added attributes for www data
* 0.2.4 - Changed user counts to handle multiple site responses, changed scheduling method to be cron-based,
*  tried to make MAC Exists and Presence Detections work across multi-site setups
* 0.2.3 - Corrected lack of a RunEvery for the 2 minute checks, corrected toLowerCase in MAC exists & Presence response handling
* 0.2.2 - Made Presence and Exists checks to be case insensitive (they force everything to lowercase when checking)
* 0.2.1 - Failed to make the new attribute everywhere, had Site when it should have been state.Site AND fixed PresenceCheck code
* 0.2.0 - Made a fundamental change to how Site is obtained. Rather than require user entry (at first) it will now get it from CurrentStats
*  Added an Override method so those with multiple sites or other reasons can change it if needed
* 0.1.8 - Disabled CSRF portion for now as it seems to be causing more errors than not
* 0.1.7 - Fix for CSRF not being set to the correct value (accidentally left an it.value instead of the correct variable)
* 0.1.6 - Revised CSRF as it appears controllers that need it do not use the same one as the UDMP
*  Major overhaul to generate params and checking for basic settings
* 0.1.5 - Addition of Status attribute to show success/failure of commands and added exception logging to Login
* 0.1.4 - Added CSRF as captured data for login and addition to all subsequent request headers
* 0.1.3 - Changed Login to be an httpPost vs async, added Alarms capability to show any un-archived alarms, minor tweaks elsewhere
* 0.1.2 - Change to Login to try to get cookie more accurately regardless of Unifi controller
* 0.1.1 - Attempt at adding support for other Unifi controller APIs. Additional logging for Presence checks.
*  Changed refresh to just be current stats (which includes online count and Unifi devices count).
*  Added a DailyCheck that performs things like getting the count of Total Clients and performing the driver update check.
*  Relabeled Refresh Rate preference to Controller Stats Refresh Rate (so it is more apparent what it is).
* 0.1.0 - Initial version
* 
* Thank you(s):
* @Barry & @ottojj for data dumps to add device types in
* @tomw for continuing to discover better ways to work with the Unifi controllers and helping me get this right
* @Cobra for inspiration on driver version checking.
* https://github.com/Art-of-WiFi/ & https://ubntwiki.com/products/software/unifi-controller/api for providing something of a basis
*/

// Returns the driver name
def DriverName(){
    return "UnifiNetworkAPI"
}

// Returns the driver version
def DriverVersion(){
    return "0.4.74"
}

// Driver Metadata
metadata{
	definition( name: "UnifiNetworkAPI", namespace: "Snell", author: "David Snell", importUrl: "https://www.drdsnell.com/projects/hubitat/drivers/UnifiNetworkAPI.groovy" ) {
		// Indicate what capabilities the device should be capable of
		capability "Sensor"
		capability "Refresh"
        capability "Actuator"
        
        // Commands
        //command "DoSomething" // Does something for development/testing purposes, should be commented before publishing
        command "Login" // Logs in to the controller to get a cookie for the session
        command "CurrentStats" // Statistics of the site including health information
        command "CheckAlarms" // Checks for any alarms not archived
        command "ArchiveAlarms" // Archives all alarms
        //command "StartSpeedtest" // Runs a speed test, but does not provide results in response, uncomment if you want to use it
        command "PresenceCheck", [
            [ name: "MAC", type: "STRING", description: "Enter a single MAC Address to check" ]
        ] // Check on clients for Presense Sensing
        command "MACExists", [
            [ name: "MAC", type: "STRING", description: "Enter a single MAC Address to check" ]
        ] // Check on clients for Presense Sensing
        command "BlockMAC", [
            [ name: "MAC", type: "STRING", description: "Enter MAC Address(es) to block (comma separated)" ]
        ] // Blocks a MAC address
        command "UnblockMAC", [
            [ name: "MAC", type: "STRING", description: "Enter MAC Address(es) to unblock (comma separated)" ]
        ] // Unblocks a MAC address
        command "Ping", [
            [ name: "IP", type: "STRING", description: "Enter IP Address to ping" ]
        ] // Pings an IP Address from the Hubitat NOT the Unifi
        command "PowerCyclePort", [
            [ name: "MAC", type: "STRING", description: "Enter MAC Address of PoE Switch" ],
            [ name: "Port", type: "INTEGER", description: "Enter PoE Port # (ex: 1, 2...)" ]
        ] // Power cycles the PoE for a specific port of a switch
        command "Reboot", [
            [ name: "Confirmation", type: "STRING", description: "Type the word Reboot to confirm intent to reboot the controller." ]
        ] // Submits a reboot command to the controller.
        command "PowerDown", [
            [ name: "Confirmation", type: "STRING", description: "Type the word PowerDown to confirm intent to power down the controller." ]
        ] // Submits a reboot command to the controller.
        /*
        command "SetLEDColor", [
            [ name: "DeviceID", type: "STRING", description: "Specify the Unifi device to set the color for" ],
            [ name: "RGB", type: "STRING", description: "Set the RGB #xxyyzz (html style) color of the specified Unifi device" ]
        ] // Sends a command to set the LED color (and turns LED on). It will turn the LED off if color is set for 000000 (black)
        */
        // Commands used for testing that CAN be enabled but really just put data into the log
        //command "CheckClients" // Checks the overall client list
        //command "CheckAccountStatus" // Gives back a set of information for the user logging in
        command "RefreshUnifiDevicesBasic" // Gets a very basic amount of information about Unifi devices connected to the controller
        
        command "RefreshUnifiDevices" // Gets a lot of data on one or all Unifi devices (overwhelms log entry with even 5 devices)
        command "RefreshSpecificUnifiDevice", [
            [ name: "MAC", type: "STRING", description: "Enter MAC Address of Unifi device to query" ]
        ] // Gets a lot of data on one Unifi device (may overwhelm the log entry)
        
        //command "RefreshOnlineClients" // Refreshes data for clients currently online
        //command "RefreshAllClients" // Refresh data for all clients that have ever connected
        //command "CurrentHealth" // Similar to CurrentStats. If enabled it dumps to the debug log (if debug or trace logging enabled).
        //command "CurrentSites" // Extremely limited info. If enabled it dumps to the debug log (if debug or trace logging enabled).
        //command "SysInfo" // Gets general system information. If enabled it dumps to the debug log (if debug or trace logging enabled).
        //command "GetAggregate" // NOT SUPPORTED YET. Provides a large amount of aggregated data used for the network dashboard on the controller.
            // https://<IP>/proxy/network/v2/api/site/default/aggregated-dashboard?scale=5minutes&start=<START_EPOCH_MILLIS>&end=<END_EPOCH_MILLIS>
        //command "RogueAP" // Polls LOTS of data on rogue Access Points detected. If enabled it dumps to the debug log (if debug or trace logging enabled).
        
        // Commands that do not appear to work on UDMP
        //command "SpectrumScan" // Polls information on wireless spectrum. If enabled it dumps to the debug log (if debug or trace logging enabled).
        /*
        command "SpectrumScan", [
            [ name: "MAC", type: "STRING", description: "Enter AP MAC Address to scan with" ]
        ] // Polls information on wireless spectrum. If enabled it dumps to the debug log (if debug or trace logging enabled).
        */
        command "ReplacePresenceChildMAC", [
            [ name: "OldMAC", type: "STRING", description: "MAC Address of the existing Presence child" ],
            [ name: "NewMAC", type: "STRING", description: "MAC Address that Presence child should be changed to" ]
        ] // Changes the MAC associated to a Presence child
        
		// Attributes for the driver itself
		attribute "DriverName", "string" // Identifies the driver being used for update purposes
		attribute "DriverVersion", "string" // Handles version for driver
        attribute "DriverStatus", "string" // Handles version notices for driver
        // Attributes for the device
        attribute "Site", "string" // Holds the value of the current Site for the controller
        attribute "Manual_Presence_Result", "string" // Lists result for MAC address when user manually checks with PresenceCheck command
        attribute "MAC_Exists_Result", "string" // Lists result for MAC address when user manually checks with MACExists command
        attribute "Total_Clients", "number" // Total number of devices that have been seen by the UDMP
        attribute "Online_Clients", "number" // Number of currently online devices
        attribute "Unifi_Devices", "number" // Number of Unifi devices reported
        attribute "Last_Login", "string" // Time that the last login was performed
        attribute "Last_Refresh", "string" // Time that the last refresh was performed
        attribute "Last_Presence_Check", "string" // Time that the last presence check was performed
        attribute "wlan-Health", "string" // Health of the wlan (Wireless network)
        attribute "wan-Health", "string" // Health of the wan (ISP)
        attribute "www-Health", "string" // Health of the www (Internet)
        attribute "lan-Health", "string" // Health of the lan (Wired Network)
        attribute "vpn-Health", "string" // Health of the vpn (VPN may list unknown if no VPN is configured)
        attribute "CPU", "number" // % of CPU usage
        attribute "Memory", "number" // % of memory usage
        attribute "Uptime", "string" // String showing days and hours of uptime
        attribute "Alarms", "list" // List of alarms that have not been archived
        attribute "Status", "string" // Meant to show success/failure of commands performed
        attribute "Upload Speed", "string" // 
        attribute "Download Speed", "string" // 
        attribute "Latency", "string" // 
        attribute "Ping Result", "string" // Holds the result of a Ping attempt, feature in Hubitat as of 2.2.7
    }
	preferences{
		section{
            if( ShowAllPreferences || ShowAllPreferences == null ){ // Show the preferences options
				input( type: "string", name: "MACPresence", title: "<b>MAC Address(s) to Presence Check (separate with ; and must be 10 or less addresses)</b>", required: false )
                input( type: "enum", name: "RefreshRate", title: "<b>Controller Stats Refresh Rate</b>", required: false, multiple: false, options: [ "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "3 hours", "Manual" ], defaultValue: "Manual" )
    			input( type: "enum", name: "LogType", title: "<b>Enable Logging?</b>", required: false, multiple: false, options: [ "None", "Info", "Debug", "Trace" ], defaultValue: "Info" )
                input( type: "string", name: "UnifiURL", title: "<font color='FF0000'><b>Unifi Controller IP/Hostname</b></font>", required: true )
				input( type: "string", name: "Username", title: "<font color='FF0000'><b>Username</b></font>", required: true )
				input( type: "password", name: "Password", title: "<font color='FF0000'><b>Password</b></font>", required: true )
                input( type: "enum", name: "Controller", title: "<font color='FF0000'><b>Unifi Controller Type</b></font>", required: true, multiple: false, options: [ "Unifi Dream Machine (inc Pro)", "Other Unifi Controllers" ], defaultValue: "Unifi Dream Machine (inc Pro)", description: "Unifi Dream Machine is for all dream devices, including UDR, UDW, etc..." )
                if( Controller == "Other Unifi Controllers" ){
				    input( type: "string", name: "ControllerPort", title: "<font color='FF0000'><b>Controller Port #</b></font>", defaultValue: "8443", required: true )
                }
                input( type: "string", name: "SiteOverride", title: "<b>Override Default Site</b>", description: "Rarely used, even if Site name is changed.", defaultValue: null, required: false )
                input( type: "bool", name: "ShowAlarms", title: "<b>Show Network Alarm Data?</b>", defaultValue: true )
                input( type: "bool", name: "AdvancedCommands", title: "<b>Enable Advanced Commands?</b>", description: "Allows controller Reboot and PowerDown commands to run.", defaultValue: false )
                input( type: "bool", name: "UnifiChildren", title: "<b>Show Unifi Devices as Children?</b>", required: true, defaultValue: false )
                input( type: "bool", name: "EnableClientCheck", title: "<b>Enable Hourly Client Checks</b>", description: "Creates ClientCheck child and performs hourly checks.", defaultValue: false )
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            } else {
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            }
        }
	}
}

// Just a command to be put fixes or other oddities during development
def DoSomething(){

}

// updated is called whenever device parameters are saved
def updated(){
    Logging( "Updating...", 2 )
    if( state."Driver Status" != null ){
        state.remove( "Driver Name" )
        state.remove( "Driver Version" )
        state.remove( "Driver Status" )
        device.deleteCurrentState( "Driver Status" )
        device.deleteCurrentState( "Driver Name" )
        device.deleteCurrentState( "Driver Version" )
    }
    ProcessEvent( "DriverName", "${ DriverName() }" )
    ProcessEvent( "DriverVersion", "${ DriverVersion() }" )
    ProcessEvent( "DriverStatus", null )

    if( LogType == null ){
        LogType = "Info"
    }
    if( Controller == null ){
        Controller = "Unifi Dream Machine (inc Pro)"
    }
    
    if( ControllerPort == null ){
        if( Controller == "Other Unifi Controllers" ){
            ControllerPort = "8443"
        }
    }
    
    if( !ShowAlarms ){
        state.remove( "Alarms" )
        device.deleteCurrentState( "Alarms" )
    }
    
    // Remove child devices (except Presence and ClientCheck) if previously created and disabled
    if( UnifiChildren == false ){
        Logging( "Unifi Children disabled, removing Unifi device children", 2 )
        getChildDevices().each{
            if( ( it.deviceNetworkId.startsWith( "Presence" ) != true ) && ( it.deviceNetworkId.startsWith( "ClientCheck" ) != true ) ){ // Ignore Presence or ClientCheck children
                Logging( "Unifi Children disabled, removing ${ it.deviceNetworkId }", 2 )
                deleteChildDevice( it.deviceNetworkId )
            }
        }
    }
    
    // Remove ClientCheck child if previously created and disabled, create child if not already created and enabled
    if( EnableClientCheck == false ){
        getChildDevices().each{
            if( it.deviceNetworkId.startsWith( "ClientCheck" ) ){
                ClientCheckExists = true
                Logging( "ClientCheck disabled, removing ClientCheck child", 2 )
                deleteChildDevice( "ClientCheck" )
            }
        }
    } else {
        def ClientCheckExists = false
        getChildDevices().each{
            if( it.deviceNetworkId.startsWith( "ClientCheck" ) ){
                ClientCheckExists = true
            }
        }
        if( ClientCheckExists != true ){
            addChild( "ClientCheck", "ClientCheck" )
        }
    }
    
    // Unschedule any existing activities so they can be reset
    unschedule()
    
    // Perform a login just to make sure everything worked and was saved properly
    // Schedule a login every 10 minutes
    def Hour = ( new Date().format( "h" ) as int )
    def Minute = ( new Date().format( "m" ) as int )
    def Second = ( new Date().format( "s" ) as int )
    
    schedule( "${ Second } 0/10 * ? * *", "Login" )
    Second = ( ( Second + 30 ) % 60 )
    
    // Check what the refresh rate is set for then run it
    switch( RefreshRate ){
        case "1 minute": // Schedule the refresh check for every minute
            schedule( "${ Second } * * ? * *", "refresh" )
            break
        case "5 minutes": // Schedule the refresh check for every 5 minutes
            schedule( "${ Second } 0/5 * ? * *", "refresh" )
            break
        case "10 minutes": // Schedule the refresh check for every 10 minutes
            schedule( "${ Second } 0/10 * ? * *", "refresh" )
            break
        case "15 minutes": // Schedule the refresh check for every 15 minutes
            schedule( "${ Second } 0/15 * ? * *", "refresh" )
            break
        case "30 minutes": // Schedule the refresh check for every 30 minutes
            schedule( "${ Second } 0/30 * ? * *", "refresh" )
            break
        case "1 hour": // Schedule the refresh check for every hour
            schedule( "${ Second } ${ Minute } * ? * *", "refresh" )
            break
        case "3 hours": // Schedule the refresh check for every 3 hours
            schedule( "${ Second } ${ Minute } 0/3 ? * *", "refresh" )
            break
        default:
            RefreshRate = "Manual"
            break
    }
    Logging( "Refresh rate: ${ RefreshRate }", 4 )
    
    // Setup scheduling for presence checking if any MAC address is entered
    Second = ( ( Second + 15 ) % 60 )
    if( MACPresence != null ){
        Logging( "MACPresense List size = ${ MACPresence.split( ";").size() }", 4 )
        if( MACPresence.split( ";").size() <= 5 ){
            schedule( "${ Second } * * * * ?", PresenceCheck )
        } else {
            if( MACPresence.split( ";").size() <= 10 ){
                schedule( "${ Second } 0/2 * * * ?", PresenceCheck )
            } else {
                ProcessEvent( "Status", "Must be 10 or less MAC addresses for Presence Checking.", 5 )
                Logging( "Too many MAC addresses to check for presence regularly.", 5 )
            }
        }
    }
    
    // Deal with SiteOverride before getting CurrentStats
    if( SiteOverride != null ){
        ProcessEvent( "Site", "${ SiteOverride }" )
    } else { // Needed to add in a default value here because controller does not respond fast enough
        ProcessEvent( "Site", "default" )
    }
    
    Second = ( ( Second + 15 ) % 60 )
    Minute = ( ( Minute + 5 ) % 60 )
    // Schedule checks that are only performed once a day
    schedule( "${ Second } ${ Minute } ${ Hour } ? * *", "DailyCheck" )

    DailyCheck()
    
    Logging( "Preferences Saved", 2 )
}

// refresh performs a poll of data
def refresh(){
    if( ControllerPort == null ){
        if( Controller == "Other Unifi Controllers" ){
            ControllerPort = "8443"
        }
    }
    CurrentStats()
    if( ShowAlarms ){
        CheckAlarms()
    } else {
        state.remove( "Alarms" )
        device.deleteCurrentState( "Alarms" )
    }
    ProcessEvent( "Last_Refresh", new Date() )
}

// DailyCheck is for items that do not need to be "fresh" all the time
def DailyCheck(){
    CheckForUpdate()
    pauseExecution( 5000 )
    RefreshAllClients()
    pauseExecution( 5000 )
    RefreshUnifiDevices()
    pauseExecution( 5000 )
    refresh()
}

//Log in to Unifi
def Login(){
    def Params
    if( Controller == "Unifi Dream Machine (inc Pro)" ){
        Params = [ uri: "https://${ UnifiURL }:443/api/auth/login", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", body: "{\"username\":\"${ Username }\",\"password\":\"${ Password }\",\"remember\":\"true\"}" ]
    } else {
        Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/login", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", body: "{\"username\":\"${ Username }\",\"password\":\"${ Password }\",\"remember\":\"true\"}" ]        
    }
    //Logging( "Login Params: ${ Params }", 4 )
    try{
        httpPost( Params ){ resp ->
	        switch( resp.getStatus() ){
		        case 200:
                    //Logging( "Login response = ${ resp.data }", 4 )
                    ProcessEvent( "Status", "Login successful." )
                    ProcessEvent( "Last_Login", new Date() )
                    def Cookie
                    resp.getHeaders().each{
                        //Logging( "Login Response Header = ${ it.value }", 4 )
                        if( ( it.value.split( '=' )[ 0 ].toString() == "unifises" ) || ( it.value.split( '=' )[ 0 ].toString() == "TOKEN" ) || ( it.value.split( '=' )[ 0 ].toString() == "UOS_TOKEN" ) ){
                            Cookie = resp.getHeaders().'Set-Cookie'
                            if( Controller == "Unifi Dream Machine (inc Pro)" ){
                                Cookie = Cookie.split( ";" )[ 0 ] + ";"
                            } else {
                                Cookie = Cookie.split( ";" )[ 0 ]
                            }
                            ProcessState( "Cookie", Cookie )
                        } else {
                            def CSRF
                            if( Controller == "Unifi Dream Machine (inc Pro)" ){
                                CSRF = it as String
                                if( CSRF.split( ':' )[ 0 ].toUpperCase() == "X-CSRF-TOKEN" ){
                                //if( ( CSRF.split( ':' )[ 0 ] == "X-CSRF-Token" ) || ( CSRF.split( ':' )[ 0 ] == "X-Csrf-Token" ) ){
                                   ProcessState( "CSRF", it.value )
                                }
                            } else {
                                if( it.value.split( '=' )[ 0 ].toString() == "csrf_token" ){
                                    CSRF = it.value.split( ';' )[ 0 ].split( '=' )[ 1 ]
                                    ProcessState( "CSRF", CSRF )
                                }
                            }
                        }
                    }
                    // Resetting Presence check and 1 minute refresh rate
                    def Second = ( new Date().format( "s" ) as int )
                    Second = ( (Second + 5) % 60 )
                    if( RefreshRate == "1 minute" ){ // Reschedule the refresh check if it is every minute
                        schedule( "${ Second } * * ? * *", "refresh" )
                    }
                    // Setup scheduling for presence checking if any MAC address is entered
                    if( MACPresence != null ){
                        Logging( "MACPresense List size = ${ MACPresence.split( ";").size() }", 4 )
                        if( MACPresence.split( ";").size() <= 5 ){
                            schedule( "${ Second } * * * * ?", PresenceCheck )
                        } else if( MACPresence.split( ";").size() <= 10 ){
                            schedule( "${ Second } 0/2 * * * ?", PresenceCheck )
                        } else {
                            ProcessEvent( "Status", "Must be 10 or less MAC addresses for Presence Checking.", 5 )
                            Logging( "Too many MAC addresses to check for presence regularly.", 5 )
                        }
                    }
			        break
                case 408:
                    Logging( "Request Timeout", 3 )
			        break
		        default:
			        Logging( "Error logging in to controller: ${ resp.status }", 4 )
			        break
	        }
        }
    } catch( Exception e ){
        Logging( "Exception when performing Login: ${ e }", 5 )
    }
}

// Check if polling OK is a simple set of tests to make sure all needed information is available before polling
def PollingOK( Message = null ){
    def OK = false
    if( state.Site != null ){
        if( ( state.Cookie != null ) ){
            OK = true
        } else {
            Logging( "No Cookie available for authentication, must Login", 5 )
            ProcessEvent( "Status", "No Cookie, please Login again." )
        }
    } else {
        Logging( "Site unknown, run CurrentStats or fill the Override Site preference.", 5 )
        ProcessEvent( "Status", "Site unknown, run CurrentStats or fill the Override Site preference." )
    }
    return OK
}

// MAC address pattern checking, per @jbaruch
//private final Pattern MACAddressPattern = Pattern.compile(/(?i)^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$/)
boolean ValidMACAddress( String MAC ) {
    MAC = MAC.trim()
    def MACPattern = ~/(?i)^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$/
    if( MACPattern.matcher( MAC ).matches() ){
        return true
    } else {
        return false
    }
}

// Replaces the MAC address associated with a Presence child
// Based on idea from @ritchierich
def ReplacePresenceChildMAC( String OldMAC, String NewMAC ){
    OldMAC = OldMAC.trim().toLowerCase()
    NewMAC = NewMAC.trim().toLowerCase()
    if( OldMAC != NewMAC ){
        if( ValidMACAddress( OldMAC ) ){
            if( ValidMACAddress( NewMAC ) ){
                if( getChildDevice( "Presence ${ NewMAC }" ) == null ){
                    if( getChildDevice( "Presence ${ OldMAC }" ) != null ){
                        getChildDevice( "Presence ${ OldMAC }" ).setDeviceNetworkId( "Presence ${ NewMAC }" )
                        if( getChildDevice( "Presence ${ NewMAC }" ).name == "Presence ${ OldMAC }" ){
                            getChildDevice( "Presence ${ NewMAC }" ).name = "Presence ${ NewMAC }"
                        }
                        if( getChildDevice( "Presence ${ NewMAC }" ).label == "Presence ${ OldMAC }" ){
                            getChildDevice( "Presence ${ NewMAC }" ).label = "Presence ${ NewMAC }"
                        }
                        if( MACPresence != null ){
                            if( MACPresence.find( OldMAC ) != null ){
                                if( MACPresence.find( NewMAC ) == null ){
                                    device.updateSetting( "MACPresence", [ value:"${ settings.MACPresence.replace( OldMAC, NewMAC ) }", type:"string" ] )
                                } else {
                                    Logging( "MACPresence already had new MAC ( ${ NewMAC } ) listed", 3 )
                                }
                            } else {
                                Logging( "MACPresence did not have the old MAC ( ${ OldMAC } ) listed", 3 )
                            }
                        } else {
                            Logging( "MACPresence did not have any MACs listed", 3 )
                        }
                    } else {
                        Logging( "ReplacePresenceChildMAC did not find existing device = Presence ${ OldMAC }", 5 )
                    }
                } else {
                    Logging( "ReplacePresenceChildMAC overlapped existing device = Presence ${ NewMAC }", 5 )
                }
            } else {
                Logging( "Invalid new MAC address for ReplacePresenceChildMAC = ${ NewMAC }", 5 )
            }
        } else {
            Logging( "Invalid old MAC address for ReplacePresenceChildMAC = ${ OldMAC }", 5 )
        }
    } else {
        Logging( "ReplacePresenceChildMAC used same MAC addresses = ${ OldMAC }", 5 )
    }
}

//Poll Unifi for current account status
def CheckAccountStatus(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/self" ), [ Method: "CheckAccountStatus" ] )
    }
}

//Poll Unifi for connected clients data
def CheckClients(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/rest/user" ), [ Method: "CheckClients" ] )
    }
}

//Poll Unifi to list a specific Unifi device's data
def RefreshSpecificUnifiDevice( MAC = null ){
    if( PollingOK() ){
        if( ValidMACAddress( MAC ) ){
            asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/device/${ MAC }" ), [ Method: "RefreshSpecificUnifiDevice" ] )
        }
    }
}

//Poll Unifi to list all Unifi devices data
def RefreshUnifiDevices(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/device" ), [ Method: "RefreshUnifiDevices" ] )
    }
}

//Poll to generate a simple list of Unifi devices
def RefreshUnifiDevicesBasic(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/device-basic" ), [ Method: "RefreshUnifiDevicesBasic" ] )
    }
}

//Poll Unifi to list site sta
def RefreshOnlineClients(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/sta" ), [ Method: "RefreshOnlineClients" ] )
    }
}

//Poll Unifi to list all clients ever
def RefreshAllClients(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/alluser" ), [ Method: "RefreshAllClients" ] )
    }
}

// Gets the statistics for the current sites
def CurrentStats(){
    if( ( state.Cookie != null ) ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/stat/sites" ), [ Method: "CurrentStats" ] )
    } else {
        Logging( "No Cookie available for authentication, must Login", 5 )
    }
}

// Gets the health for the site
def CurrentHealth(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/health" ), [ Method: "CurrentHealth" ] )
    }
}

// Gets basic stats for the sites
def CurrentSites(){
    if( ( state.Cookie != null ) ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/self/sites" ), [ Method: "CurrentSites" ] )
    } else {
        Logging( "No Cookie available for authentication, must Login", 5 )
    }
}

// Gets the SysInfo for the site
def SysInfo(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/sysinfo" ), [ Method: "SysInfo" ] )
    }
}

// Lists rogue Access Points that have been detected
def RogueAP(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/rogueap" ), [ Method: "RogueAP" ] )
    }
}

// Checks for any current (not archived) alarms on the controller
def CheckAlarms(){
    if( PollingOK() ){
        asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/rest/alarm?archived=false" ), [ Method: "CheckAlarms" ] )
    }
}

// Checks if a specific MAC address exists
def MACExists( MAC = null ){
    if( PollingOK() ){
        if( MAC != null ){
            def MACs = MAC.split( ";" )
            MACs.each{
                asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/user/${ it.toLowerCase() }" ), [ Method: "MACExists", MAC: "${ it.toLowerCase() }" ] )
                pauseExecution( 2000 )
            }
        } else {
            Logging( "No MAC to check for.", 3 )
            ProcessEvent( "Status", "No MAC to check." )
        }
    }
}

// Checks if a specific MAC address is online
def PresenceCheck( MAC = null ){
    if( PollingOK() ){
        def MACs
        def Params
        def TempManual = false
        if( MAC != null ){
            MACs = MAC.split( ";" )
            TempManual = true
        } else if( MACPresence != null ){
            MACs = MACPresence.split( ";" )
        }
        if( MACs != null ){
            MACs.each{
                Params = GenerateNetworkParams( "api/s/${ state.Site }/stat/sta/${ it.toLowerCase() }" )
                Logging( "PresenceCheck Params = ${ Params }", 4 )
                asynchttpGet( "ReceiveData", GenerateNetworkParams( "api/s/${ state.Site }/stat/sta/${ it.toLowerCase() }" ), [ Method: "PresenceCheck", MAC: "${ it.toLowerCase() }", Manual: TempManual ] )
                pauseExecution( 5000 )
            }
        } else {
            Logging( "No devices to check presence for.", 3 )
            ProcessEvent( "Status", "No devices to check presence for." )
        }
    }
}

// Checks if a client is online
def CheckClient( String MAC, Value ){
    if( PollingOK() ){
        if( ( ValidMACAddress( MAC ) ) && ( Value != null ) ){
            def Params = GenerateNetworkParams( "api/s/${ state.Site }/stat/sta/${ MAC.toLowerCase() }" )
            Logging( "CheckClient MAC = ${ MAC.toLowerCase() } & Number = ${ Value }", 4 )
            Logging( "CheckClient Params = ${ Params }", 4 )
            asynchttpGet( "ReceiveData", Params, [ Method: "CheckClient", ClientMAC: "${ MAC.toLowerCase() }", ClientNumber: "${ Value }" ] )
        } else {
            Logging( "No client to check or MAC invalid.", 3 )
            ProcessEvent( "Status", "No client to check." )
        }
    }
}

// Archive alarms on the controller
def ArchiveAlarms(){
    if( PollingOK() ){
        def Params
        if( Controller == "Unifi Dream Machine (inc Pro)" ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/cmd/evtmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{ \"cmd\":\"archive-all-alarms\" }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/cmd/evtmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{ \"cmd\":\"archive-all-alarms\" }" ]
        }
        Logging( "Params = ${ Params }", 4 )
        try{
            httpPost( Params ){ resp ->
                switch( resp.getStatus() ){
                    case 200:
                        ProcessEvent( "Status", "ArchiveAlarms succeeded" )
                        ProcessEvent( "Alarms", [], null, true )
                        Logging( "ArchiveAlarms succeeded = ${ resp.data }", 4 )
                        break
                    default:
                        Logging( "ArchiveAlarms received ${ resp.getStatus() }", 4 )
                        break
                }
            }
        } catch( Exception e ){
            Logging( "ArchiveAlarms failed due to ${ e }", 5 )
        }
    }
}

// Start speedtest
def StartSpeedtest(){
    if( PollingOK() ){
        def Params
        if( Controller == "Unifi Dream Machine (inc Pro)" ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/cmd/devmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{ \"cmd\":\"speedtest\" }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/cmd/devmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{ \"cmd\":\"speedtest\" }" ]
        }
        Logging( "Params = ${ Params }", 4 )
        try{
            httpPost( Params ){ resp ->
                switch( resp.getStatus() ){
                    case 200:
                        ProcessEvent( "Status", "StartSpeedtest succeeded" )
                        Logging( "StartSpeedtest succeeded = ${ resp.data }", 3 )
                        break
                    default:
                        Logging( "StartSpeedtest received ${ resp.getStatus() }", 3 )
                        break
                }
            }
        } catch( Exception e ){
            Logging( "StartSpeedtest failed due to ${ e }", 5 )
        }
    }
}

// Gets the Spectrum Scanning information for the site - DOES NOT APPEAR TO WORK
def SpectrumScan(){
    if( PollingOK() ){
        def Params
        if( Controller == "Unifi Dream Machine (inc Pro)" ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/stat/spectrumscan", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/stat/spectrumscan", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
        }
        Logging( "Params = ${ Params }", 4 )
        try{
            httpPost( Params ){ resp ->
                switch( resp.getStatus() ){
                    case 200:
                        ProcessEvent( "Status", "SpectrumScan ran" )
                        Logging( "SpectrumScan succeeded = ${ resp.data }", 3 )
                        break
                    default:
                        Logging( "SpectrumScan received ${ resp.getStatus() }", 3 )
                        break
                }
            }
        } catch( Exception e ){
            Logging( "SpectrumScan failed due to ${ e }", 5 )
        }
    }
}

// Block Single MAC
def BlockSingleMAC( String MAC ){
    if( ( MAC.size() == 17 ) && ( ValidMACAddress( MAC.toLowerCase() ) ) ){
        def Params
        if( Controller == "Unifi Dream Machine (inc Pro)" ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/cmd/stamgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{\"cmd\":\"block-sta\",\"mac\":\"${ MAC.toLowerCase() }\"}" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/cmd/stamgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{\"cmd\":\"block-sta\",\"mac\":\"${ MAC.toLowerCase() }\"}" ]
        }
        Logging( "BlockMAC Params = ${ Params }", 4 )
        try{
            httpPost( Params ){ resp ->
                switch( resp.getStatus() ){
                    case 200:
                        ProcessEvent( "Status", "Blocked ${ MAC }" )
                        Logging( "BlockMAC succeeded = ${ resp.data }", 2 )
                        break
                    default:
                        Logging( "BlockMAC received ${ resp.getStatus() }", 2 )
                        break
                }
            }
        } catch( Exception e ){
            Logging( "BlockMAC failed due to ${ e }", 5 )
        }
    } else {
        Logging( "Invalid MAC address ${ MAC } entered to block.", 3 )
        ProcessEvent( "Status", "Invalid MAC address ${ MAC } entered to block." )
    }
}

// Block MAC address(es)
def BlockMAC( String MAC ){
    if( PollingOK() ){
        if( MAC.size() == 17 ){
            BlockSingleMAC( MAC )
        } else if( MAC.size() > 17 ){
            def TempMAC = MAC.split( "," )
            TempMAC.each(){
                BlockSingleMAC( it.toLowerCase() )
                pauseExecution( 1000 )
            }
        }
    }
}

// Unblock Single MAC
def UnblockSingleMAC( String MAC ){
    if( ( MAC.size() == 17 ) && ( ValidMACAddress( MAC.toLowerCase() ) ) ){
        def Params
        if( Controller == "Unifi Dream Machine (inc Pro)" ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/cmd/stamgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{\"cmd\":\"unblock-sta\",\"mac\":\"${ MAC.toLowerCase() }\"}" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/cmd/stamgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "{\"cmd\":\"unblock-sta\",\"mac\":\"${ MAC.toLowerCase() }\"}" ]
        }
        Logging( "UnblockMAC Params = ${ Params }", 4 )
        try{
            httpPost( Params ){ resp ->
                switch( resp.getStatus() ){
                    case 200:
                        ProcessEvent( "Status", "Unblocked ${ MAC }" )
                        Logging( "UnblockMAC succeeded = ${ resp.data }", 2 )
                        break
                    default:
                        Logging( "UnblockMAC received ${ resp.getStatus() }", 2 )
                        break
                }
            }
        } catch( Exception e ){
            Logging( "UnblockMAC failed due to ${ e }", 5 )
        }
    } else {
        Logging( "Invalid MAC address ${ MAC } entered to unblock.", 3 )
        ProcessEvent( "Status", "Invalid MAC address ${ MAC } entered to unblock." )
    }
}

// Unblock MAC address(es)
def UnblockMAC( String MAC ){
    if( PollingOK() ){
        if( MAC.size() == 17 ){
            UnblockSingleMAC( MAC )
        } else if( MAC.size() > 17 ){
            def TempMAC = MAC.split( "," )
            TempMAC.each(){
                UnblockSingleMAC( it.toLowerCase() )
                pauseExecution( 1000 )
            }
        }
    }
}

// Trigger power cycling of a device's port
def PowerCyclePort( String MAC, int Port ){
    if( PollingOK() ){
        //RunDevMgrCommand( "PowerCyclePort", "${ MAC }", "{\"cmd\":\"power-cycle\",\"mac\":\"${ MAC.toLowerCase() }\",\"port_idx\":${ Port }}" )
        RunDevMgrCommand( "PowerCyclePort", "${ MAC }", "{\"mac\":\"${ MAC.toLowerCase() }\",\"cmd\":\"power-cycle\",\"port_idx\":${ Port }}" )
    }
}

// Reboot the controller
def Reboot( String Confirmation ){
    if( PollingOK() ){
        if( AdvancedCommands ){
            if( Confirmation == "Reboot" ){
                def Params
                if( Controller == "Unifi Dream Machine (inc Pro)" ){
                    Params = [ uri: "https://${ UnifiURL }:443/api/system/reboot", ignoreSSLIssues: true, headers: [ Referer: "https://${ UnifiURL }/settings/advanced", Host: "${ UnifiURL }", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
                } else {
                    Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/system/reboot", ignoreSSLIssues: true, headers: [ Referer: "https://${ UnifiURL }/settings/advanced", Host: "${ UnifiURL }", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
                }
                Logging( "Reboot Params = ${ Params }", 4 )
                try{
                    httpPost( Params ){ resp ->
                        switch( resp.getStatus() ){
		                    case 200:
                            case 204:
                                ProcessEvent( "Status", "Reboot command sent" )
                                Logging( "Reboot command sent = ${ resp.data }", 4 )
                                break
                            default:
                                Logging( "Reboot command error ${ resp.getStatus() }", 3 )
                                break
                        }
                    }
                } catch( Exception e ){
                    Logging( "Reboot failed due to ${ e }", 5 )
                }
            } else {
                Logging( "Reboot confirmation incorrect. Reboot command ignored.", 5 )
                ProcessEvent( "Status", "Reboot confirmation incorrect. Reboot command ignored." )
            }
        } else {
            Logging( "Advanced Commands disabled. Reboot command ignored.", 2 )
            ProcessEvent( "Status", "Advanced Commands disabled. Reboot command ignored." )
        }
    }
}

// Power down the controller
def PowerDown( String Confirmation ){
    if( PollingOK() ){
        if( AdvancedCommands ){
            if( Confirmation == "PowerDown" ){
                def Params
                if( Controller == "Unifi Dream Machine (inc Pro)" ){
                    Params = [ uri: "https://${ UnifiURL }:443/api/system/poweroff", ignoreSSLIssues: true, headers: [ Referer: "https://${ UnifiURL }/settings/advanced", Host: "${ UnifiURL }", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
                } else {
                    Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/system/poweroff", ignoreSSLIssues: true, headers: [ Referer: "https://${ UnifiURL }/settings/advanced", Host: "${ UnifiURL }", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ] ]
                }
                Logging( "PowerDown Params = ${ Params }", 4 )
                try{
                    httpPost( Params ){ resp ->
                        switch( resp.getStatus() ){
		                    case 200:
                            case 204:
                                ProcessEvent( "Status", "PowerDown command sent" )
                                Logging( "PowerDown command sent = ${ resp.data }", 4 )
                                break
                            default:
                                Logging( "PowerDown command error ${ resp.getStatus() }", 3 )
                                break
                        }
                    }
                } catch( Exception e ){
                    Logging( "PowerDown failed due to ${ e }", 5 )
                }
            } else {
                Logging( "PowerDown confirmation incorrect. PowerDown command ignored.", 5 )
                ProcessEvent( "Status", "PowerDown confirmation incorrect. PowerDown command ignored." )
            }
        } else {
            Logging( "Advanced Commands disabled. PowerDown command ignored.", 2 )
            ProcessEvent( "Status", "Advanced Commands disabled. PowerDown command ignored." )
        }
    }
}

// Sets the state of the PoE port
def SetPortState( String DNI, String ChildID, String MAC, int PortNumber, String State ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    if( PollingOK() ){
        def CurrentOverrides = getChildDevice( DNI ).ReturnState( "Port Overrides" )
        def TempOverrides = ""
        def TempPortOn
        if( State == "auto" ){
            TempPortOn = true
        } else {
            TempPortOn = false
        }
        Logging( "Current Overrides are: ${ CurrentOverrides }", 4 )
        if( CurrentOverrides == null ){
            TempOverrides = "{\"port_idx\":${ PortNumber },\"poe_mode\":\"${ State }\",\"port_poe\":${ TempPortOn } }"
        } else {
            def NumOverrides = CurrentOverrides.size()
            def Place = 0
            def Handled = false
            CurrentOverrides.each(){
                if( it.name != null ){
                    TempOverrides += "{\"name\":\"${ it.name }\",\"port_idx\":${ it.port_idx }"
                } else {
                    TempOverrides += "{\"port_idx\":${ it.port_idx }"
                }
                if( it.port_idx == PortNumber ){
                    TempOverrides += ",\"poe_mode\":\"${ State }\",\"port_poe\":${ TempPortOn }"
                    Handled = true
                } else {
                    if( it.poe_mode != null ){
                        TempOverrides += ",\"poe_mode\":\"${ it.poe_mode }\""
                    }
                }
                it.each(){
                    switch( it.key ){
                        case "name":
                        case "port_idx":
                        case "poe_mode":
                            break
                        case "mac_table":
                        case "excluded_networkconf_ids":
                        case "port_security_mac_address":
                            TempOverrides += ",\"${ it.key }\":${ it.value }"
                            break
                        default:
                            def TempValue = it.value as String
                            if( ( it.value == true ) || ( it.value == false ) ){
                                TempOverrides += ",\"${ it.key }\":${ it.value }"
                            } else if( ( TempValue ) && ( TempValue.isNumber() ) ){
                                TempOverrides += ",\"${ it.key }\":${ it.value }"
                            } else {
                                TempOverrides += ",\"${ it.key }\":\"${ it.value }\""
                            }
                            break
                    }
                }
                TempOverrides += "}"
                Place ++
                if( Place < NumOverrides ){
                    TempOverrides += ","    
                }
            }
            if( Handled == false ){
                TempOverrides += ",{\"port_idx\":${ PortNumber },\"poe_mode\":\"${ State }\",\"port_poe\":${ TempPortOn }}"
            }
        }
        Logging( "Temp Overrides are: ${ TempOverrides }", 4 )
        asynchttpPut( "ReceiveData", GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"port_overrides\":[${ TempOverrides }]}" ), [ Method: "SetPortState", DNI: "${ DNI }", MAC: "${ MAC }", ChildID: "${ ChildID }", Value: "${ Port } = ${ State }" ] )
    }
}

// Turn on/off power outlet
def PowerOutlet( String DNI, String ChildID, String MAC, int Outlet, String OnOff ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    // /proxy/network/api/s/default/rest/device/639b69fc4cae7b076a61e569
    if( PollingOK() ){
        def CurrentOverrides = getChildDevice( DNI ).ReturnState( "Outlet Overrides" )
        Logging( "Current Overrides = ${ CurrentOverrides }", 4 )
        def TempOverrides = ""
        if( CurrentOverrides == null ){
            if( OnOff == "On" ){
                TempOverrides = "{\"outlet_overrides\":[{\"index\":${ Outlet },\"relay_state\":true}]}"
            } else {
                TempOverrides = "{\"outlet_overrides\":[{\"index\":${ Outlet },\"relay_state\":false}]}"
            }
        } else {
            def NumOverrides = CurrentOverrides.size()
            def Place = 0
            def Handled = false
            CurrentOverrides.each(){
                if( it.name != null ){
                    TempOverrides += "{\"index\":${ it.index },\"name\":\"${ it.name }\""
                } else {
                    TempOverrides += "{\"index\":${ it.index }"
                }
                if( it.cycle_enabled != null ){
                    TempOverrides += ",\"cycle_enabled\":${ it.cycle_enabled }"
                }
                if( it.index == Outlet ){
                    if( OnOff == "On" ){
                        TempOverrides += ",\"relay_state\":true"
                    } else {
                        TempOverrides += ",\"relay_state\":false"
                    }
                    Handled = true
                } else {
                    if( it.relay_state != null ){
                        TempOverrides += ",\"relay_state\":${ it.relay_state }"
                    }
                    Handled = true
                }
                it.each(){
                    switch( it.key ){
                        case "name":
                        case "index":
                        case "cycle_enabled":
                        case "relay_state":
                            break
                        default:
                            def TempValue = it.value as String
                            if( ( it.value == true ) || ( it.value == false ) ){
                                TempOverrides += ",\"${ it.key }\":${ it.value }"
                            } else if( TempValue.isNumber() ){
                                TempOverrides += ",\"${ it.key }\":${ it.value }"
                            } else {
                                TempOverrides += ",\"${ it.key }\":\"${ it.value }\""
                            }
                            break
                    }
                }
                TempOverrides += "}"
                Place ++
                if( Place < NumOverrides ){
                    TempOverrides += ","    
                }
            }
            if( Handled == false ){
                TempOverrides += ",{\"index\":${ Outlet },"
                if( OnOff == "On" ){
                    TempOverrides += ",\"relay_state\":true}"
                } else {
                    TempOverrides += ",\"relay_state\":false}"
                }
            }
        }
        def Params = GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"outlet_overrides\":[${ TempOverrides }]}" )
        asynchttpPut( "ReceiveData", Params, [ Method: "PowerOutlet", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "Outlet ${ Outlet } = ${ OnOff }" ] )
    }
}

// Turn on/off the device's LED
def SetLEDOnOff( String DNI, String ChildID, String MAC, String OnOff ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    if( PollingOK() ){
        if( OnOff == "On" ){
            asynchttpPut( "ReceiveData", GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"led_override\": \"on\" }" ), [ Method: "SetLEDOnOff", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "${ OnOff }" ] )
        } else {
            asynchttpPut( "ReceiveData", GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"led_override\": \"off\" }" ), [ Method: "SetLEDOnOff", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "${ OnOff }" ] )   
        }
    }
}

// Set the device's LED Color
def SetLEDColor( String DNI, String ChildID, String MAC, String RGB ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"led_override_color\": \"#${ RGB.toLowerCase() }\" }" ), [ Method: "SetLEDColor", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "${ RGB }" ] )
    }
}

// Set the device's LCM Brightness
def SetLCMBrightness( String DNI, String Name, String ChildID, Number Brightness ){
    def Attempt = "api/s/${ state.Site }/rest/device${ ChildID }"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkParams( "${ Attempt }", "{\"name\":\"${ Name }\",\"lcm_brightness\": ${ Brightness }, \"lcm_brightness_override\":true }" ), [ Method: "SetDisplayBrightness", DNI: "${ DNI }", ChildID: "${ ChildID }" ] )
    }
}

// Set the device's LED Brightness
def SetLEDBrightness( String DNI, String ChildID, String MAC, Number Brightness ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkSettingParams( "${ Attempt }", "${ ChildID }", "${ MAC }", "{\"led_override_color_brightness\": ${ Brightness } }" ), [ Method: "SetLEDBrightness", DNI: "${ DNI }", ChildID: "${ ChildID }" ] )
    }
}

// Set the device's Etherlighting 
def SetDeviceEtherlighting( String DNI, String Name, String ChildID, Map Etherlighting ){
    def Attempt = "api/s/${ state.Site }/rest/device/${ ChildID }"
    Logging( "Etherlighting Map = ${ Etherlighting }", 3 )
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkParams( "${ Attempt }", "{\"name\":\"${ Name }\",\"ether_lighting\": { \"mode:\": \"${ Etherlighting.Mode }\", \"brightness:\": ${ Etherlighting.Brightness }, \"behavior:\": \"${ Etherlighting.Behavior }\" } }" ), [ Method: "SetDeviceEtherlighting", DNI: "${ DNI }", ChildID: "${ ChildID }", Map: Etherlighting ] )
    }
}

// Set an Etherlighting speed override
def OverrideEtherSpeed( String RGB, String Speed ){
    def Attempt = "api/s/${ state.Site }/set/setting/ether_lighting"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkParams( "${ Attempt }", "{\"key\":\"ether_lighting\",\"speed_overrides\":[{ \"raw_color_hex:\": ${ RGB }, \"key\":\"${ Speed }\" }] }" ), [ Method: "OverrideEtherSpeed", RGB: "${ RGB }", Key: "${ Key }" ]  )
    }
}

// Send a general httpPut Command
def SendPutCommand( String DNI, String ChildID, String Command ){
    def Attempt = "api/s/${ state.Site }/rest/device/${ ChildID }"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkParams( "${ Attempt }", "${ Command }" ), [ Method: "SendPutCommand", DNI: "${ DNI }", ChildID: "${ ChildID }" ] )
    }
}

// Turn on/off an RPS Port
def SetRPSPortState( String DNI, String ChildID, int PortNumber, String PortState ){
    def Attempt = "api/s/${ state.Site }/rest/device"
    if( PollingOK() ){
        asynchttpPut( "ReceiveData", GenerateNetworkCommandParams( "${ Attempt }", "${ ChildID }", "{ \"rps_override\": { \"rps_port_table\": [ { \"port_idx\": ${ PortNumber }, \"port_mode\": \"${ PortState }\" } ] } }" ), [ Method: "SetRPSPortOnOff", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "${ PortNumber } = ${ PortState }" ] )
    }
}

// Set device to perform locate/identify function
def LocateDevice( String MAC, String OnOff ){
    if( PollingOK() ){
        if( OnOff == "On" ){
            RunDevMgrCommand( "LocateDevice", "${ MAC }", "{\"cmd\":\"set-locate\",\"mac\":\"${ MAC.toLowerCase() }\"}" )
        } else {
            RunDevMgrCommand( "LocateDevice", "${ MAC }", "{\"cmd\":\"unset-locate\",\"mac\":\"${ MAC.toLowerCase() }\"}" )
        }
    }
}

// Trigger power cycling of a device outlet
def PowerCycleOutlet( String MAC, int Outlet ){
    if( PollingOK() ){
        RunDevMgrCommand( "PowerCycleOutlet", "${ MAC }", "{\"mac\":\"${ MAC.toLowerCase() }\",\"outlet_table\":[{\"index\":${ Outlet }}],\"cmd\":\"outlet-ctl\"}" )
    }
}

// RestartDevice attempts to restart a particular device
def RestartDevice( String MAC ){
    if( PollingOK() ){
        RunDevMgrCommand( "RestartDevice", "${ MAC }", "{\"mac\":\"${ MAC }\",\"reboot_type\":\"soft\",\"cmd\":\"restart\"}" )
    }
}

// Configure child device settings based on Preferences
def SendChildSettings( String DNI, String ChildID, String Value ){
    def Attempt = "api/s/${ state.Site }/rest/device/"
    asynchttpPut( "ReceiveData", GenerateNetworkManageParams( "${ Attempt }", "${ ChildID }", "${ Value }" ), [ Method: "SendChildSettings", DNI: "${ DNI }", ChildID: "${ ChildID }", Value: "${ Value }" ] )
}

// Generate Network Params assembles the parameters to be sent to the controller rather than repeat so much of it
def GenerateNetworkParams( String Path, String Data = null ){
	def Params
	if( Controller == "Unifi Dream Machine (inc Pro)" ){
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/${ Path }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }" ], data:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:443/proxy/network/${ Path }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }" ] ]
        }
	} else {
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/${ Path }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }" ], data:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/${ Path }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }" ] ]
        }
	}
    Logging( "Parameters = ${ Params }", 4 )
	return Params
}

// GenerateNetworkCommandParams assembles the parameters to be sent to the controller rather than repeat so much of it
def GenerateNetworkCommandParams( String Path, String ChildID, String Data = null ){
	def Params
	if( Controller == "Unifi Dream Machine (inc Pro)" ){
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/${ ChildID }/general", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ], body:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/${ ChildID }/general", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ] ]
        }
	} else {
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/${ ChildID }/general", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ], body:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/${ ChildID }/general", Origin: "https://${ UnifiURL }", Accept: "*/*", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ] ]
        }
	}
    Logging( "Parameters = ${ Params }", 4 )
	return Params
}

// GenerateNetworkManageParams assembles the parameters to be sent to the controller rather than repeat so much of it
def GenerateNetworkManageParams( String Path, String ChildID, String Data ){
	def Params
	if( Controller == "Unifi Dream Machine (inc Pro)" ){
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/properties/${ ChildID }/device", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ], body:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/properties/${ ChildID }/device", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ] ]
        }
	}
    Logging( "Parameters = ${ Params }", 4 )
	return Params
}

// GenerateNetworkSettingParams assembles the parameters to be sent to the controller rather than repeat so much of it
def GenerateNetworkSettingParams( String Path, String ChildID, String MAC, String Data ){
	def Params
	if( Controller == "Unifi Dream Machine (inc Pro)" ){
        if( Data != null ){
            Params = [ uri: "https://${ UnifiURL }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/properties/${ MAC }/settings", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ], body:"${ Data }" ]
        } else {
            Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/proxy/network/${ Path }/${ ChildID }", ignoreSSLIssues: true, requestContentType: "application/json", contentType: "application/json", headers: [ Host: "${ UnifiURL }", Referer: "https://${ UnifiURL }/network/${ state.Site }/devices/properties/${ MAC }/settings", Origin: "https://${ UnifiURL }", Cookie: "${ state.Cookie }", 'X-CSRF-Token': "${ state.CSRF }" ] ]
        }
	}
    Logging( "Parameters = ${ Params }", 4 )
	return Params
}

// Run a command on the controller
def RunDevMgrCommand( String Method, String MAC, String Data ){
    if( PollingOK() ){
        if( ValidMACAddress( MAC ) ){
            def Params
            if( Controller == "Unifi Dream Machine (inc Pro)" ){
                Params = [ uri: "https://${ UnifiURL }:443/proxy/network/api/s/${ state.Site }/cmd/devmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:443", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "${ Data }" ]
            } else {
                Params = [ uri: "https://${ UnifiURL }:${ ControllerPort }/api/s/${ state.Site }/cmd/devmgr", ignoreSSLIssues: true, headers: [ Host: "${ UnifiURL }:${ ControllerPort }", Accept: "*/*", Cookie: "${ state.Cookie }", "X-CSRF-Token": "${ state.CSRF }" ], body: "${ Data }" ]
            }
            Logging( "${ Method } Params = ${ Params }", 4 )
            try{
                httpPost( Params ){ resp ->
                    switch( resp.getStatus() ){
                        case 200:
                            ProcessEvent( "Status", "${ Method } ${ MAC }" )
                            Logging( "${ Method } succeeded = ${ resp.data }", 4 )
                            break
                        default:
                            Logging( "${ Method } received ${ resp.getStatus() }", 3 )
                            break
                    }
                }
            } catch( Exception e ){
                Logging( "${ Method } failed with exception ${ e }", 4 )
            }
        } else {
            Logging( "No valid MAC address entered.", 3 )
            ProcessEvent( "Status", "No valid MAC address entered." )
        }
    }
}

// Handles receiving the data from various commands
def ReceiveData( resp, data ){
	switch( resp.getStatus() ){
		case 200:
            Logging( "Received ${ resp.data }", 4 )
            def Json = parseJson( resp.data )
            ProcessEvent( "Status", "${ data.Method } successful." )
            switch( data.Method ){
                case "CheckAccountStatus":
                    def SiteNumber = 0
                    Json.data.each(){
                        if( SiteNumber == 0 ){
                            ProcessState( "Site ID", it.site_id )
                            ProcessState( "Site Name", it.site_name )
                        } else {
                            ProcessState( "Site ${ ( SiteNumber + 2 ) } ID", it.site_id )
                            ProcessState( "Site ${ ( SiteNumber + 2 ) } Name", it.site_name )
                        }
                        SiteNumber = SiteNumber + 1
                    }
                    break
                case "RefreshOnlineClients":
                    ProcessEvent( "Online_Clients", Json.data.size() )
                    break
                case "RefreshAllClients":
                case "CheckClients":
                    ProcessEvent( "Total_Clients", Json.data.size() )
                    break
                case "RefreshSpecificUnifiDevice":
                case "RefreshUnifiDevicesBasic":
                case "RefreshUnifiDevices":
                    if( data.Method != "RefreshSpecificUnifiDevice" ){
                        ProcessEvent( "Unifi_Devices", Json.data.size() )
                    }
                    if( UnifiChildren ){
                        Json.data.each(){
                            if( it.model != null ){
                                switch( it.model ){
                                    case "USPM48P": // Unifi 48 Port Pro Max PoE Switch (USPM48P)
                                        if( getChildDevice( "USPM48P ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM48P ${ it.mac }", "Device Type", "USPM48P" )
                                        }
                                        ProcessData( "USPM48P ${ it.mac }", it )
                                        break
                                    case "USPM24P": // Unifi 24 Port Pro Max PoE Switch (USPM24P)
                                        if( getChildDevice( "USPM24P ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM24P ${ it.mac }", "Device Type", "USPM24P" )
                                        }
                                        ProcessData( "USPM24P ${ it.mac }", it )
                                        break
                                    case "USPM16P": // Unifi 16 Port Pro Max PoE Switch (USPM16P)
                                        if( getChildDevice( "USPM16P ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM16P ${ it.mac }", "Device Type", "USPM16P" )
                                        }
                                        ProcessData( "USPM16P ${ it.mac }", it )
                                        break
                                    case "USPM16": // Unifi 16 Port Pro Max non-PoE Switch (USPM16)
                                        if( getChildDevice( "USPM16 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM16 ${ it.mac }", "Device Type", "USPM16" )
                                        }
                                        ProcessData( "USPM16 ${ it.mac }", it )
                                        break
                                    case "USPM48": // Unifi 48 Port Pro Max non-PoE Switch (USPM48)
                                        if( getChildDevice( "USPM48 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM48 ${ it.mac }", "Device Type", "USPM48" )
                                        }
                                        ProcessData( "USPM48 ${ it.mac }", it )
                                        break
                                    case "USPM24": // Unifi 24 Port Pro Max non-PoE Switch (USPM24)
                                        if( getChildDevice( "USPM24 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPM24 ${ it.mac }", "Device Type", "USPM24" )
                                        }
                                        ProcessData( "USPM24 ${ it.mac }", it )
                                        break
                                    case "US48PRO": // Unifi 48 Port PRO PoE Switch (USW48PoE)
                                        if( getChildDevice( "USW48PoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW48PoE ${ it.mac }", "Device Type", "USW48PoE" )
                                        }
                                        ProcessData( "USW48PoE ${ it.mac }", it )
                                        break
                                    case "US48": // Unifi 48 Port non-PoE Switch (USW48)
                                        if( getChildDevice( "USW48 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW48 ${ it.mac }", "Device Type", "USW48" )
                                        }
                                        ProcessData( "USW48 ${ it.mac }", it )
                                        break
                                    case "USL24P250": // Unifi 24 Port 250W PoE Switch (USW24PoE)
                                    case "US24P250": // Unifi 24 Port 250W PoE Switch (USW24PoE)
                                    case "USL24P": // Unifi 24 Port PoE Switch (USW24PoE)
                                    case "US624P": // Unifi 24 Port Enterprise PoE Switch (USW24PoE)
                                        if( getChildDevice( "USW24PoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW24PoE ${ it.mac }", "Device Type", "USW24PoE" )
                                        }
                                        ProcessData( "USW24PoE ${ it.mac }", it )
                                        break
                                    case "US24": // Unifi 24 Port non-PoE Switch (USW24)
                                    case "USL24": // Unifi 24 Port non-PoE Switch (USW24)
                                        if( getChildDevice( "USW24 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW24 ${ it.mac }", "Device Type", "USW24" )
                                        }
                                        ProcessData( "USW24 ${ it.mac }", it )
                                        break
                                    case "US16P150": // Unifi 16 Port 150W PoE Switch (USW16PoE)
                                        if( getChildDevice( "USW16PoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW16PoE ${ it.mac }", "Device Type", "USW16PoE" )
                                        }
                                        ProcessData( "USW16PoE ${ it.mac }", it )
                                        break
                                    case "USL16LP": // Unifi 16 Port Lite PoE Switch (USW16LPoE)
                                    case "USL16P": // Unifi 16 Port Lite PoE Switch (USW16LPoE)
                                        if( getChildDevice( "USW16LPoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW16LPoE ${ it.mac }", "Device Type", "USW16LPoE" )
                                        }
                                        ProcessData( "USW16LPoE ${ it.mac }", it )
                                        break
                                    case "US16": // Unifi 16 Port non-PoE Switch (USW16)
                                    case "USL16": // Unifi 16 Port non-PoE Switch (USW16)
                                        if( getChildDevice( "USW16 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW16 ${ it.mac }", "Device Type", "USW16" )
                                        }
                                        ProcessData( "USW16 ${ it.mac }", it )
                                        break
                                    case "USWED37": // Unifi 8 Port Flex 2.5G PoE Switch (USW8PoE)
                                    case "US8P150": // Unifi 8 Port 150W PoE Switch (USW8PoE)
                                        if( getChildDevice( "USW8PoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW8PoE ${ it.mac }", "Device Type", "USW8PoE" )
                                        }
                                        ProcessData( "USW8PoE ${ it.mac }", it )
                                        break
                                    case "US8P60": // Unifi 8 Port 60W PoE Switch (USW8PoE)
                                        if( getChildDevice( "USW8PoE60 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW8PoE60 ${ it.mac }", "Device Type", "USW8PoE60" )
                                        }
                                        ProcessData( "USW8PoE60 ${ it.mac }", it )
                                        break
                                    case "USL8LPB": // Unifi Lite 8 Port PoE Switch
                                    case "USL8LP": // Unifi Lite 8 Port PoE Switch (USW-Lite-8-PoE)
                                        if( getChildDevice( "USW8LPoE ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW8LPoE ${ it.mac }", "Device Type", "USW8LPoE" )
                                        }
                                        ProcessData( "USW8LPoE ${ it.mac }", it )
                                        break
                                    case "US8": // Unifi 8 Port non-PoE Switch (USW8)
                                    case "USC8": // Unifi 8 Port Switch with PoE Passthrough (US8)
                                    case "USL8": // Unifi 8 Port non-PoE Switch (USW8)
                                        if( getChildDevice( "USW8 ${ it.mac }" ) == null ){
                                            PostEventToChild( "USW8 ${ it.mac }", "Device Type", "USW8" )
                                        }
                                        ProcessData( "USW8 ${ it.mac }", it )
                                        break
                                    case "U6ENT": // U6 Enterprise In Wall
                                    case "U6IW": // U6 In Wall
                                    case "UHDIW": // Unifi HD In Wall
                                        if( getChildDevice( "UHDIW ${ it.mac }" ) == null ){
                                            PostEventToChild( "UHDIW ${ it.mac }", "Device Type", "UHDIW" )
                                        }
                                        ProcessData( "UHDIW ${ it.mac }", it )
                                        break
                                    case "U7IW": // Unifi AC In Wall
                                        if( getChildDevice( "ACIW ${ it.mac }" ) == null ){
                                            PostEventToChild( "ACIW ${ it.mac }", "Device Type", "ACIW" )
                                        }
                                        ProcessData( "ACIW ${ it.mac }", it )
                                        break
                                    case "U7NHD": // Unifi Access Point nanoHD
                                    case "UAP6MP": // Unifi Access Point 6 Pro
                                    case "UALR6v2": // Unifi Access Point 6 LR
                                    case "UAL6": // Unifi Access Point 6 Lite
                                    case "U7P": // Unifi AC Pro
                                    case "U7MP": // UAP-AC-M-Pro
                                    case "U7PG2": // UAP-AC-Pro
                                    case "U7LT": // UAP-AC-Lite
                                    case "U7HD": // UAP-AC-HD
                                    case "UCXG": // UAP-XG
                                    case "BZ2LR": // Unifi AC-LR
                                    case "U6M": // Unifi U6 Mesh
                                    case "UAPL6": // U6+
                                    case "U7PRO": // U7 Pro
                                        if( getChildDevice( "AP ${ it.mac }" ) == null ){
                                            PostEventToChild( "AP ${ it.mac }", "Device Type", "AP" )
                                        }
                                        ProcessData( "AP ${ it.mac }", it )
                                        break
                                    case "U7MSH": // UAP-AC-M
                                    case "U7PIW": // U7 Pro In Wall
                                        if( getChildDevice( "BasicAP ${ it.mac }" ) == null ){
                                            PostEventToChild( "BasicAP ${ it.mac }", "Device Type", "BasicAP" )
                                        }
                                        ProcessData( "BasicAP ${ it.mac }", it )
                                        break
                                    case "USPRPS": // Unifi Redundant Power System
                                        if( getChildDevice( "RPS ${ it.mac }" ) == null ){
                                            PostEventToChild( "RPS ${ it.mac }", "Device Type", "RPS" )
                                        }
                                        ProcessData( "RPS ${ it.mac }", it )
                                        break
                                    case "UDR": // Unifi Dream Router
                                        if( getChildDevice( "UDR ${ it.mac }" ) == null ){
                                            PostEventToChild( "UDR ${ it.mac }", "Device Type", "UDR" )
                                        }
                                        ProcessData( "UDR ${ it.mac }", it )
                                        break
                                    case "UDMPROSE": // Unifi Dream Machine Pro SE
                                        if( getChildDevice( "UDMPSE ${ it.mac }" ) == null ){
                                            PostEventToChild( "UDMPSE ${ it.mac }", "Device Type", "UDMPSE" )
                                        }
                                        ProcessData( "UDMPSE ${ it.mac }", it )
                                        break
                                    case "UDMPRO": // Unifi Dream Machine Pro
                                        if( getChildDevice( "UDMP ${ it.mac }" ) == null ){
                                            PostEventToChild( "UDMP ${ it.mac }", "Device Type", "UDMP" )
                                        }
                                        ProcessData( "UDMP ${ it.mac }", it )
                                        break
                                    case "UDM": // Unifi Dream Machine
                                        if( getChildDevice( "UDM ${ it.mac }" ) == null ){
                                            PostEventToChild( "UDM ${ it.mac }", "Device Type", "UDM" )
                                        }
                                        ProcessData( "UDM ${ it.mac }", it )
                                        break
                                    case "UX": // Unifi Express Gateway
                                        if( getChildDevice( "UX ${ it.mac }" ) == null ){
                                            PostEventToChild( "UX ${ it.mac }", "Device Type", "UX" )
                                        }
                                        ProcessData( "UX ${ it.mac }", it )
                                        break
                                    case "UP1": // Unifi Smart Plug
                                        if( getChildDevice( "Plug ${ it.mac }" ) == null ){
                                            PostEventToChild( "Plug ${ it.mac }", "Device Type", "Plug" )
                                        }
                                        ProcessData( "Plug ${ it.mac }", it )
                                        break
                                    case "UP6": // Unifi Power Strip
                                        if( getChildDevice( "UP6 ${ it.mac }" ) == null ){
                                            PostEventToChild( "UP6 ${ it.mac }", "Device Type", "UP6" )
                                        }
                                        ProcessData( "UP6 ${ it.mac }", it )
                                        break
                                    case "USPPDUP": // Unifi SmartPower Pro PDU
                                        if( getChildDevice( "USPPDUP ${ it.mac }" ) == null ){
                                            PostEventToChild( "USPPDUP ${ it.mac }", "Device Type", "USPPDUP" )
                                        }
                                        ProcessData( "USPPDUP ${ it.mac }", it )
                                        break
                                    case "USWED35": // Unifi Switch Flex Mini 2.5G
                                    case "USMINI": // Unifi Switch Mini
                                        if( getChildDevice( "USMINI ${ it.mac }" ) == null ){
                                            PostEventToChild( "USMINI ${ it.mac }", "Device Type", "USMINI" )
                                        }
                                        ProcessData( "USMINI ${ it.mac }", it )
                                        break
                                    case "USF5P": // Unifi Switch Flex
                                        if( getChildDevice( "USF5P ${ it.mac }" ) == null ){
                                            PostEventToChild( "USF5P ${ it.mac }", "Device Type", "USF5P" )
                                        }
                                        ProcessData( "USF5P ${ it.mac }", it )
                                        break
                                    case "UGW3": // Security Gateway 3
                                        if( getChildDevice( "UGW3 ${ it.mac }" ) == null ){
                                            PostEventToChild( "UGW3 ${ it.mac }", "Device Type", "UGW3" )
                                        }
                                        ProcessData( "UGW3 ${ it.mac }", it )
                                        break
                                    case "UGW3": // Security Gateway 3
                                        if( getChildDevice( "UGW3 ${ it.mac }" ) == null ){
                                            PostEventToChild( "UGW3 ${ it.mac }", "Device Type", "UGW3" )
                                        }
                                        ProcessData( "UGW3 ${ it.mac }", it )
                                        break
                                    case "UGW4": // Security Gateway Pro
                                        if( getChildDevice( "UGW4 ${ it.mac }" ) == null ){
                                            PostEventToChild( "UGW4 ${ it.mac }", "Device Type", "UGW4" )
                                        }
                                        ProcessData( "UGW4 ${ it.mac }", it )
                                        break
                                    case "USG": // Unifi Security Gateway
                                        if( getChildDevice( "USG ${ it.mac }" ) == null ){
                                            PostEventToChild( "USG ${ it.mac }", "Device Type", "USG" )
                                        }
                                        ProcessData( "USG ${ it.mac }", it )
                                        break
                                    case "UXG": // Unifi Gateway Lite
                                        if( getChildDevice( "UXG ${ it.mac }" ) == null ){
                                            PostEventToChild( "UXG ${ it.mac }", "Device Type", "UXG" )
                                        }
                                        ProcessData( "UXG ${ it.mac }", it )
                                        break
                                    case "UXGB": // Unifi Gateway Max
                                        if( getChildDevice( "UXGB ${ it.mac }" ) == null ){
                                            PostEventToChild( "UXGB ${ it.mac }", "Device Type", "UXGB" )
                                        }
                                        ProcessData( "UXGB ${ it.mac }", it )
                                        break
                                    case "UCGMAX": // Cloud Gateway Max
                                    	if( getChildDevice( "UCGMax ${ it.mac }" ) == null ){
                                            PostEventToChild( "UCGMax ${ it.mac }", "Device Type", "UCGMax" )
                                        }
                                        ProcessData( "UCGMax ${ it.mac }", it )
                                        break
                                    case "USAGGPRO": // Aggregate Pro
                                        if( getChildDevice( "USAGGPRO ${ it.mac }" ) == null ){
                                            PostEventToChild( "USAGGPRO ${ it.mac }", "Device Type", "USAGGPRO" )
                                        }
                                        ProcessData( "USAGGPRO ${ it.mac }", it )
                                        break
                                    default:
                                        Logging( "Unidentified Unifi ${ it.model } Device = ${ it }", 3 )
                                        if( getChildDevice( "Generic ${ it.mac }" ) == null ){
                                            PostEventToChild( "Generic ${ it.mac }", "Device Type", "Generic" )
                                        }
                                        ProcessData( "Generic ${ it.mac }", it )
                                        break
                                }
                            }
                        }
                    }
                    break
                case "CheckClient":
                    def ClientMAC = ""
                    def ConnectedMAC = ""
                    def TempMap = getChildDevice( "ClientCheck" ).ReturnState( "Client ${ data.ClientNumber }" )
                    if( TempMap == null ){
                        TempMap = [ MAC: "${ data.ClientMAC }", Status: "unknown", Name: "unknown", LastSeen: "unknown", ConnectedToMAC: "unknown", ConnectedToName: "unknown", Speed: 0, SignalStrength: "unknown", SSID: "unknown", Uptime: "unknown" ]
                    }
                    Logging( "CheckClient Response = ${ Json }", 4 )
                    Json.data.each(){
                        if( it.mac != null ){
                            TempMap.Status = "Online"
                        } else {
                            TempMap.Status = "Offline"
                        }
                        if( it.name != null ){
                            TempMap.Name = it.name
                        } else if( it.hostname != null ){
                            TempMap.Name = it.hostname
                        } else if( TempMap.Name == null ){
                            TempMap.Name = "unknown"
                        }
                        if( it.last_seen != null ){
                            TempMap.LastSeen = ConvertEpochToDate( "${ it.last_seen }" )
                        }
                        if( it.is_wired ){
                            TempMap.'Connected To MAC' = it.sw_mac
                            TempMap.'Signal Strength' = "Wired"
                            TempMap.SSID = "NA"
                        } else {
                            TempMap.'Connected To MAC' = it.ap_mac
                            TempMap.'Signal Strength' = it.signal
                            TempMap.SSID = it.essid
                        }
                        if( it.uptime != null ){
                            def TempUptime = it.uptime as int
                            def TempUptimeDays = Math.round( TempUptime / 86400 )
                            def TempUptimeHours = Math.round( ( TempUptime % 86400 ) / 3600 )
                            def TempUptimeMinutes = Math.round( ( TempUptime % 3600 ) / 60 )
                            def TempUptimeString = "${ TempUptimeDays } Day"
                            if( TempUptimeDays != 1 ){
                                TempUptimeString += "s"
                            }
                            TempUptimeString += " ${ TempUptimeHours } Hour"
                            if( TempUptimeHours != 1 ){
                                TempUptimeString += "s"
                            }
                            TempUptimeString += " ${ TempUptimeMinutes } Minute"
                            if( TempUptimeMinutes != 1 ){
                                TempUptimeString += "s"
                            }
                            TempMap.Uptime = TempUptimeString
                        } else {
                            TempMap.Uptime = "unknown"
                        }
                        getChildDevices().each{
                            if( it.deviceNetworkId.indexOf( TempMap.'Connected To MAC' ) != -1 ){
                                TempMap.'Connected To Name' = it.displayName
                            }
                        }
                        if( it.wired_rate_mbps != null ){
                            TempMap.Speed = it.wired_rate_mbps
                        }
                    }
                    PostEventToChild( "ClientCheck", "Client ${ data.ClientNumber }", TempMap )
                    break
                case "PresenceCheck":
                    ProcessEvent( "Last_Presence_Check", new Date() )
                    def ClientMAC = ""
                    def ConnectedMAC = ""
                    if( data.Manual ){
                        Json.data.each(){
                            if( it.mac != null ){
                                ClientMAC = it.mac
                                ProcessEvent( "Manual_Presence_Result", "${ ClientMAC } is present as of ${ ConvertEpochToDate( "${ it.last_seen }" ) }" )
                            }
							if( MACPresence != null ){
                                if( MACPresence.toLowerCase().indexOf( "${ ClientMAC }" ) >= 0 ){
                                    PostEventToChild( "Presence ${ ClientMAC }", "presence", "present" )
                                    if( getChildDevice( "Presence ${ ClientMAC }" ).label == null ){
                                        getChildDevice( "Presence ${ ClientMAC }" ).label = it.name
                                    }
                                    PostEventToChild( "Presence ${ ClientMAC }", "LastSeen", ConvertEpochToDate( "${ it.last_seen }" ) )
                                    if( it.is_wired ){
                                        ConnectedMAC = it.sw_mac
                                    } else {
                                        ConnectedMAC = it.ap_mac
                                    }
                                    PostEventToChild( "Presence ${ ClientMAC }", "Connected To MAC", ConnectedMAC )
                                    getChildDevices().each{
                                        if( it.deviceNetworkId.indexOf( "${ ConnectedMAC }" ) != -1 ){
                                            PostEventToChild( "Presence ${ ClientMAC }", "Connected To Name", it.displayName )
                                        }
                                    }
                                }
                            }
                            if( it.uptime != null ){
                                def TempUptime = it.uptime as int
                                def TempUptimeDays = ( TempUptime / 86400 ) as int
                                PostEventToChild( "Presence ${ ClientMAC }", "UptimeDays", TempUptimeDays, "days" )
                                def TempUptimeHours = ( ( TempUptime % 86400 ) / 3600 ) as int
                                PostEventToChild( "Presence ${ ClientMAC }", "UptimeHours", ( TempUptime / 3600 ) as int, "hours" )
                                def TempUptimeMinutes = ( ( TempUptime % 3600 ) / 60 ) as int
                                PostEventToChild( "Presence ${ ClientMAC }", "UptimeMinutes", ( TempUptime / 60 ) as int, "minutes" )
                                PostEventToChild( "Presence ${ ClientMAC }", "UptimeSeconds", TempUptime, "seconds" )
                                def TempUptimeString = "${ TempUptimeDays } Day"
                                if( TempUptimeDays != 1 ){
                                    TempUptimeString += "s"
                                }
                                TempUptimeString += " ${ TempUptimeHours } Hour"
                                if( TempUptimeHours != 1 ){
                                    TempUptimeString += "s"
                                }
                                TempUptimeString += " ${ TempUptimeMinutes } Minute"
                                if( TempUptimeMinutes != 1 ){
                                    TempUptimeString += "s"
                                }
                                PostEventToChild( "Presence ${ ClientMAC }", "Uptime", TempUptimeString )
                            } else {
                                PostEventToChild( "Presence ${ ClientMAC }", "Uptime", "unknown" )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeDays", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeHours", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeMinutes", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeSeconds", 0 )
                            }
                        }
                    } else {
                        Json.data.each(){
                            ClientMAC = it.mac
                            if( MACPresence.toLowerCase().indexOf( "${ ClientMAC }" ) >= 0 ){
                                PostEventToChild( "Presence ${ ClientMAC }", "presence", "present" )
                                if( getChildDevice( "Presence ${ ClientMAC }" ).label == null ){
                                    getChildDevice( "Presence ${ ClientMAC }" ).label = it.name
                                }
                                PostEventToChild( "Presence ${ ClientMAC }", "LastSeen", ConvertEpochToDate( "${ it.last_seen }" ) )
                                if( it.is_wired ){
                                    ConnectedMAC = it.sw_mac
                                } else {
                                    ConnectedMAC = it.ap_mac
                                }
                                PostEventToChild( "Presence ${ ClientMAC }", "Connected To MAC", ConnectedMAC )
                                if( it.uptime != null ){
                                    def TempUptime = it.uptime as int
                                    def TempUptimeDays = ( TempUptime / 86400 ) as int
                                    PostEventToChild( "Presence ${ ClientMAC }", "UptimeDays", TempUptimeDays, "days" )
                                    def TempUptimeHours = ( ( TempUptime % 86400 ) / 3600 ) as int
                                    PostEventToChild( "Presence ${ ClientMAC }", "UptimeHours", ( TempUptime / 3600 ) as int, "hours" )
                                    def TempUptimeMinutes = ( ( TempUptime % 3600 ) / 60 ) as int
                                    PostEventToChild( "Presence ${ ClientMAC }", "UptimeMinutes", ( TempUptime / 60 ) as int, "minutes" )
                                    PostEventToChild( "Presence ${ ClientMAC }", "UptimeSeconds", TempUptime, "seconds" )
                                    def TempUptimeString = "${ TempUptimeDays } Day"
                                    if( TempUptimeDays != 1 ){
                                        TempUptimeString += "s"
                                    }
                                    TempUptimeString += " ${ TempUptimeHours } Hour"
                                    if( TempUptimeHours != 1 ){
                                        TempUptimeString += "s"
                                    }
                                    TempUptimeString += " ${ TempUptimeMinutes } Minute"
                                    if( TempUptimeMinutes != 1 ){
                                        TempUptimeString += "s"
                                    }
                                    PostEventToChild( "Presence ${ ClientMAC }", "Uptime", TempUptimeString )
                                } else {
                                    PostEventToChild( "Presence ${ ClientMAC }", "Uptime", "unknown" )
                                    PostEventToChild( "Presence ${ data.MAC }", "UptimeDays", 0 )
                                    PostEventToChild( "Presence ${ data.MAC }", "UptimeHours", 0 )
                                    PostEventToChild( "Presence ${ data.MAC }", "UptimeMinutes", 0 )
                                    PostEventToChild( "Presence ${ data.MAC }", "UptimeSeconds", 0 )
                                }
                                getChildDevices().each{
                                    if( it.deviceNetworkId.indexOf( "${ ConnectedMAC }" ) != -1 ){
                                        PostEventToChild( "Presence ${ ClientMAC }", "Connected To Name", it.displayName )
                                    }
                                }
                            }
                        }  
                    }
                    break
                case "MACExists":
                    Json.data.each(){
                        ProcessEvent( "MAC_Exists_Result", "${ it.mac } exists as of ${ ConvertEpochToDate( "${ it.last_seen }" ) }" )
                    }
                    break
                case "CurrentStats":
                    def TempOnline = 0
                    def TempUnifiDevices = 0
                    if( state.Site == null ){
                        ProcessEvent( "Site", "${ Json.data[ 0 ].name }" )
                    }
                    Json.data.each(){
                        def TempSite = "${ it.name }"
                        if( it.health != null ){
                            it.health.each(){
                                if( TempSite == state.Site ){
                                    ProcessEvent( "${ it.subsystem }-Health", "${ it.status }" )
                                }
                                if( it.num_adopted != null ){
                                    TempUnifiDevices = TempUnifiDevices + ( it.num_adopted ) as int
                                }
                                if( it.num_user != null ){
                                    TempOnline = TempOnline + ( it.num_user ) as int
                                }
                                ProcessState( "${ TempSite }-${ it.subsystem }-Health", "${ it.status }" )
                                if( it.subsystem != null ){
                                    switch( it.subsystem ){
                                        case "wan":
                                            if( it."gw_system-stats" != null ){
                                                if( it."gw_system-stats".cpu != null ){
                                                    ProcessEvent( "CPU", it."gw_system-stats".cpu, "%" )
                                                }
                                                if( it."gw_system-stats".mem != null ){
                                                    ProcessEvent( "Memory", it."gw_system-stats".mem, "%" )
                                                }
                                                if( it."gw_system-stats".uptime != null ){
                                                    def TempSeconds = it."gw_system-stats".uptime as int
                                                    def TempMinutes = ( TempSeconds / 60 ) as int
                                                    def TempHours = ( TempMinutes / 60 ) as int
                                                    def TempDays = ( TempHours / 24 ) as int
                                                    TempMinutes = ( TempMinutes % 60 )
                                                    TempHours = ( TempHours % 24 )
                                                    ProcessEvent( "Uptime", "${ TempDays } days ${ TempHours } hours ${ TempMinutes } minutes" )
                                                }
                                            }
                                            break
                                        case "wlan":
                                            break
                                        case "lan":
                                            break
                                        case "www":
                                            if( it.xput_up != null ){
                                                ProcessEvent( "Upload Speed", "${ it.xput_up }Mbps" )
                                            }
                                            if( it.xput_down != null ){
                                                ProcessEvent( "Download Speed", "${ it.xput_down }Mbps" )
                                            }
                                            if( it.latency != null ){
                                                ProcessEvent( "Latency", "${ it.latency }" )
                                            }
                                            break
                                        case "vpn":
                                            break
                                        default:
                                            Logging( "Unhandled SubSystem ${ it.subsystem } reported", 3 )
                                            break
                                    }
                                }
                            }
                        }
                    }
                    ProcessEvent( "Online_Clients", TempOnline )
                    if( Controller == "Unifi Dream Machine (inc Pro)" ){
                        ProcessEvent( "Unifi_Devices", ( TempUnifiDevices - 1 ) )
                    } else {
                        ProcessEvent( "Unifi_Devices", TempUnifiDevices )
                    }
                    break
                case "CheckAlarms":
                    if( Json.data != null ){
                        def AlarmNumber = 1
                        def TempAlarms = []
                        Json.data.each{
                            if( it.msg != null ){
                                TempAlarms.push( [ "${ it.msg }" ] )
                                Logging( "Alarm: ${ it.msg }", 4 )
                            }
                            AlarmNumber++
                        }
                        ProcessEvent( "Alarms", TempAlarms )
                    } else {
                        ProcessEvent( "Alarms", "No active alarms" )
                    }
                    break
                case "PowerOutlet":
                    if( UnifiChildren ){
                        if( Json.data != null ){
                            if( Json.data[ 0 ] != null ){
                                if( Json.data[ 0 ].size() > 0 ){
                                    //ProcessData( "${ data.DNI }", Json.data[ 0 ] )
                                    if( Json.data[ 0 ].outlet_overrides != null ){
                                        if( Json.data[ 0 ].outlet_overrides.size() > 0 ){
                                            PostStateToChild( "${ data.DNI }", "Outlet Overrides",  Json.data[ 0 ].outlet_overrides )
                                            switch( getChildDevice( "${ data.DNI }" ).ReturnState( "Model" ) ){
												case "UP1":
													if( Json.data[ 0 ].outlet_overrides.relay_state ){
														PostEventToChild( "${ data.DNI }", "switch", "on" )
													} else {
														PostEventToChild( "${ data.DNI }", "switch", "off" )
													}
													break
												case "UP6":
													Json.data[ 0 ].outlet_overrides.each(){
														if( it.index != null && it.relay_state != null ){
															if( it.index != 7 ){
																if( it.relay_state ){
																	PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "on" )
																} else {
																	PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "off" )
																}
															} else {
																if( it.relay_state ){
																	PostEventToChild( "${ data.DNI }", "USB Ports", "on" )
																} else {
																	PostEventToChild( "${ data.DNI }", "USB Ports", "off" )
																}
															}
														}
													}
													break
												case "USPPDUP":
													Json.data[ 0 ].outlet_overrides.each(){
														if( it.index != null && it.relay_state != null ){
															if( it.index > 4 ){
																if( it.relay_state ){
																	PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "on" )
																} else {
																	PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "off" )
																}
															} else {
																if( it.relay_state ){
															        PostEventToChild( "${ data.DNI }", "USB Port ${ it.index }", "on" )
																} else {
																	PostEventToChild( "${ data.DNI }", "USB Port ${ it.index }", "off" )
																}
															}
														}
													}
													break
												default:
													Json.data[ 0 ].outlet_overrides.each(){
														if( it.index != null && it.relay_state != null ){
															if( it.relay_state ){
																PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "on" )
															} else {
																PostEventToChild( "${ data.DNI }", "Outlet ${ it.index }", "off" )
															}
														}
													}
													break
											}
											break
                                        }
                                    }
                                } else {
                                    Logging( "No data returned, no changes made on controller side.", 4 )
                                }
                            } else {
                                Logging( "No data returned, no changes made on controller side.", 4 )
                            }
                        } else {
                            Logging( "No data returned, no changes made on controller side.", 4 )
                        }
                    }
                    break
                case "SetPortState":
                    if( Json.data.port_overrides != null ){
                        if( Json.data.port_overrides.size() > 0 ){
                            PostStateToChild( "${ data.DNI }", "Port Overrides", Json.data.port_overrides )
                        } else {
                            Logging( "SetPortState for ${ data.DNI } successfull, but no overrides returned (likely no changes made)", 1 )
                        }
                    }
                    RefreshSpecificUnifiDevice( data.MAC )
                    break
                case "SetRPSPortOnOff":
                case "SetLEDOnOff":
                case "SetLEDColor":
                case "PowerCycleOutlet":
                case "PowerCyclePort":
                case "SetLEDBrightness":
                case "SendChildSettings":
                    if( UnifiChildren ){
                        if( Json.data != null ){
                            if( Json.data[ 0 ] != null ){
                                if( Json.data[ 0 ].size() > 0 ){
                                    ProcessData( "${ data.DNI }", Json.data[ 0 ] )
                                } else {
                                    Logging( "No data returned, no changes made on controller side.", 4 )
                                }
                            } else {
                                Logging( "No data returned, no changes made on controller side.", 4 )
                            }
                        } else {
                            Logging( "No data returned, no changes made on controller side.", 4 )
                        }
                    }
                    break
                case "SetDeviceEtherlighting":
                    def TempMap = [ Mode: "unknown", Brightness: 100, Behavior: "unknown" ]
                    if( Json.data[ 0 ].ether_lighting.mode != null ){
                        TempMap.Mode = Json.data[ 0 ].ether_lighting.mode
                    } else {
                        TempMap.Mode = data.Map.Mode
                    }
                    if( Json.data[ 0 ].ether_lighting.brightness != null ){
                        TempMap.Brightness = Json.data[ 0 ].ether_lighting.brightness
                    } else {
                        TempMap.Brightness = data.Map.Brightness
                    }
                    if( Json.data[ 0 ].ether_lighting.behavior != null ){
                        TempMap.Behavior = Json.data[ 0 ].ether_lighting.behavior
                    } else {
                        TempMap.Behavior = data.Map.Behavior
                    }
                    PostStateToChild( "${ data.DNI }", "Etherlighting",  TempMap )
                    break
                default:
                    Logging( "Received Data for ${ data.Method }: ${ resp.data }", 3 )
                    if( UnifiChildren ){
                        if( Json.data[ 0 ].size() > 0 ){
                            ProcessData( "${ data.DNI }", Json.data[ 0 ] )
                        } else {
                            Logging( "${ data.Method } attempt failed.", 5 )
                        }
                    }
			        break
            }
            break
        case 400: // Bad request
            switch( data.Method ){
                // Controller sends a 400 when a MAC is offline for presence checks & MAC exists checks
                case "PresenceCheck":
                    ProcessEvent( "Last_Presence_Check", new Date() )
                    if( data.Manual ){
                        ProcessEvent( "Manual_Presence_Result", "${ data.MAC } is not present as of ${ new Date() }" )
                        if( MACPresence != null ){
                            if( MACPresence.toLowerCase().indexOf( data.MAC ) >= 0 ){
                                PostEventToChild( "Presence ${ data.MAC }", "presence", "not present" )
                                PostEventToChild( "Presence ${ data.MAC }", "Uptime", "0 Days 0 Hours 0 Minutes" )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeDays", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeHours", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeMinutes", 0 )
                                PostEventToChild( "Presence ${ data.MAC }", "UptimeSeconds", 0 )
                            }
                        }
                    } else {
                        if( MACPresence.toLowerCase().indexOf( data.MAC ) >= 0 ){
                            PostEventToChild( "Presence ${ data.MAC }", "presence", "not present" )
                            PostEventToChild( "Presence ${ data.MAC }", "Uptime", "0 Days 0 Hours 0 Minutes" )
                            PostEventToChild( "Presence ${ data.MAC }", "UptimeDays", 0 )
                            PostEventToChild( "Presence ${ data.MAC }", "UptimeHours", 0 )
                            PostEventToChild( "Presence ${ data.MAC }", "UptimeMinutes", 0 )
                            PostEventToChild( "Presence ${ data.MAC }", "UptimeSeconds", 0 )
                        }
                    }
                    break
                case "ClientCheck":
                    Logging( "CheckClient ${ data.ClientMAC } = Offline", 4 )
                    def ClientMAC = ""
                    def ConnectedMAC = ""
                    def TempMap = getChildDevice( "ClientCheck" ).ReturnState( "Client ${ data.ClientNumber }" )
                    if( TempMap == null ){
                        TempMap = [ MAC: "${ data.ClientMAC }", Status: "unknown", Name: "unknown", LastSeen: "unknown", ConnectedToMAC: "unknown", ConnectedToName: "unknown", Speed: 0, SignalStrength: "unknown", SSID: "unknown", Uptime: "unknown" ]
                    }
                    TempMap.Status = "Offline"
                    TempMap.'Connected To MAC' = "None"
                    TempMap.'Signal Strength' = "unknown"
                    TempMap.SSID = "NA"
                    PostEventToChild( "ClientCheck", "Client ${ data.ClientNumber }", TempMap )
                    break
                case "MACExists":
                    Logging( "Bad Request = MAC ${ data.MAC } does not exist on controller.", 4 )
                    ProcessEvent( "MAC_Exists_Result", "${ data.MAC } does not exist as of ${ new Date() }" )
                    break
                default:
                    Logging( "Bad Request for ${ data.Method }", 3 )
			        break
            }
            break
        case 401:
            ProcessEvent( "Status", "${ data.Method } Unauthorized, please Login again" )
            Logging( "Unauthorized for ${ data.Method } please Login again", 5 )
			break
        case 404:
            ProcessEvent( "Status", "${ data.Method } Page not found error" )
            Logging( "Page not found for ${ data.Method }", 5 )
			break
        case 408:
            ProcessEvent( "Status", "Request timeout for ${ data.Method }" )
            switch( data.Method ){
                case "PresenceCheck":
                    Logging( "Request Timeout checking if ${ data.MAC } is present", 5 )
                    break
                case "MACExists":
                    Logging( "Request Timeout checking if ${ data.MAC } exists", 5 )
                    break
                default:
                    Logging( "Request Timeout for ${ data.Method }", 5 )
			        break
            }
			break
		default:
            ProcessEvent( "Status", "Error ${ resp.status } connecting for ${ data.Method }" )
			Logging( "Error connecting to Unifi Controller: ${ resp.status } for ${ data.Method }", 5 )
			break
	}
}

// Process data coming in for a device
def ProcessData( String Device, data ){
    //Logging( "${ Device } Data: ${ data }", 4 )
    data.each(){
        if( it.key != null ){
            switch( it.key ){
                case "name":
                    if( it.value != null ){
                        if( getChildDevice( "${ Device }" ) != null ){
                            if( getChildDevice( "${ Device }" ).label == null ){
                                getChildDevice( "${ Device }" ).label = it.value
                            }
                            PostStateToChild( "${ Device }", "DeviceName", it.value )
                        } else {
                            PostStateToChild( "${ Device }", "DeviceName", it.value )
                            getChildDevice( "${ Device }" ).label = it.value
                        }
                    }
                    break
                case "lcm_brightness_override":
                    if( it.value ){
                        PostStateToChild( "${ Device }", "LCMBrightnessOverride", "true" )
                    } else {
                        PostStateToChild( "${ Device }", "LCMBrightnessOverride", "false" )
                    }
                    break
                case "model":
                    PostStateToChild( "${ Device }", "Model", it.value )
                    break
                case "hostname":
                    PostStateToChild( "${ Device }", "Hostname", it.value )
                    break
                case "overheating":
                    if( it.value ){
                        PostEventToChild( "${ Device }", "Overheating", "true" )
                    } else {
                        PostEventToChild( "${ Device }", "Overheating", "false" )
                    }
                    break
                case "ip":
                    PostEventToChild( "${ Device }", "IP", it.value )
                    break
                case "version":
                    PostEventToChild( "${ Device }", "DeviceVersion", it.value )
                    break
                case "lcm_brightness":
                    PostStateToChild( "${ Device }", "LCMBrightness", it.value )
                    break
                case "lcm_night_mode_begins":
                    PostStateToChild( "${ Device }", "LCMNightStart", it.value )
                    break
                case "lcm_night_mode_ends":
                    PostStateToChild( "${ Device }", "LCMNightEnd", it.value )
                    break
                case "general_temperature":
                    PostEventToChild( "${ Device }", "temperature", ConvertTemperature( "C", it.value ), "°${ location.getTemperatureScale() }" )
                    break
                case "has_fan":
                    if( it.value ){
                        PostStateToChild( "${ Device }", "Has Fan", "true" )
                    } else {
                        PostStateToChild( "${ Device }", "Has Fan", "false" )
                    }
                    break
                case "satisfaction":
                    PostEventToChild( "${ Device }", "Satisfaction", it.value )
                    break
                case "device_domain":
                    PostStateToChild( "${ Device }", "Device Domain", it.value )
                    break
                case "license_state":
                    PostStateToChild( "${ Device }", "License State", it.value )
                    break
                case "board_rev":
                    PostStateToChild( "${ Device }", "Board Revision", it.value )
                    break
                case "system-stats":
                    PostEventToChild( "${ Device }", "Memory Usage", it.value.mem, "%" )
                    PostEventToChild( "${ Device }", "CPU Usage", it.value.cpu, "%" )
                    break
                case "fan_level":
                    PostStateToChild( "${ Device }", "Fan Level", it.value )
                    break
                case "power_source":
                    PostStateToChild( "${ Device }", "Power Source", it.value )
                    break
                case "model_in_eol":
                    if( it.value ){
                        PostEventToChild( "${ Device }", "Model EOL", "true" )
                    } else {
                        PostEventToChild( "${ Device }", "Model EOL", "false" )
                    }
                    break
                case "anomalies":
                    if( it.value != -1 ){
                        PostStateToChild( "${ Device }", "Anomalies", it.value )
                    } else {
                        PostStateToChild( "${ Device }", "Anomalies", "None" )
                    }
                    break
                case "total_max_power":
                    PostStateToChild( "${ Device }", "Total Max Power", it.value )
                    break
                case "model_in_lts":
                    if( it.value ){
                        PostEventToChild( "${ Device }", "Model LTS", "true" )
                    } else {
                        PostEventToChild( "${ Device }", "Model LTS", "false" )
                    }
                    break
                case "kernel_version":
                    PostStateToChild( "${ Device }", "Kernel Version", it.value )
                    break
                case "rps":
                    if( it.value != null ){
                        if( getChildDevice( "${ Device }" ) == null ){
                            break    
                        }
                        switch( getChildDevice( "${ Device }" ).ReturnState( "Device Type" ) ){
                            case "BasicAP": // APs without Status LED Ring
                            case "AP": // APs with Status LED Ring
                            case "UP1": // Plug
                            case "Plug": // Plug
                            case "UP6": // Powerstrip
                            case "USPPDUP": // SmartPower Pro PDU
                            case "USW48": // 48 Port non-PoE Switch
                            case "USW24": // 24 Port non-PoE Switch
                            case "USW16": // 16 Port non-PoE Switch
                            case "USW8": // 8 Port non-PoE Switch
                            case "UXG": // Unifi Gateway Lite
                            case "UXGB": // Unifi Gateway Max
                            case "USG": // Unifi Security Gateway
                            case "UGW3": // Unifi Security Gateway 3
                            case "UGW4": // Unifi Security Gateway Pro
                            case "UX": // Unifi Express Gateway
                            case "UDM": // Unifi Dream Machine
                            case "UDMP": // Unifi Dream Machine Pro
                            case "UDMPSE": // Unifi Dream Machine Pro SE
                            case "UDR": // Unifi Dream Router
                            case "USMINI": // Flex Mini 5 Port Switch
                            case "USW8PoE": // 8 Port PoE Switch
                            case "USW8PoE60": // 8 Port 60W PoE Switch
                            case "USW8LPoE": // 8 Port PoE Lite Switch
                            case "USF5P": // Flex 5 Port PoE Switch
                            case "UHDIW": // In-Wall AP with PoE Port
                            //case "U6IW": // U6 In Wall
                            //case "U6ENT": // U6 Enterprise In Wall
                            case "ACIW": // AC In Wall
                            case "USAGGPRO": // Aggregate Pro
                                break
                            case "USW48PoE": // 48 Port PoE Switch
                            case "USW24PoE": // 24 Port PoE Switch
                            case "USW16PoE": // 16 Port PoE Switch
                            case "USW16LPoE": // 16 Port PoE Lite Switch
                            case "USPM48P": // 48 Port Pro Max PoE Switch
                            case "USPM24P": // 24 Port Pro Max PoE Switch
                            case "USPM16P": // 16 Port Pro Max PoE Switch
                            case "USPM48": // 48 Port Pro Max non-PoE Switch
                            case "USPM24": // 24 Port Pro Max non-PoE Switch
                            case "USPM16": // 16 Port Pro Max non-PoE Switch
                                if( it.value.rps_port_table != null ){
                                    it.value.rps_port_table.each(){
                                        def PortNum = "0"
                                        if( it.port_idx < 10 ){
                                            PortNum = "0${ it.port_idx }"
                                        } else {
                                            PortNum = "${ it.port_idx }"
                                        }
                                        if( it.port_mode == "auto" ){
                                            PostEventToChild( "${ Device }", "Port ${ it.PortNum } Status", "Auto" )
                                        } else if( it.port_mode == "disabled" ){
                                            PostEventToChild( "${ Device }", "Port ${ it.PortNum } Status", "Disabled" )
                                        }
                                        if( it.peer != null ){
                                            PostStateToChild( "${ Device }", "${ it.name } Connection", "${ it.peer.model }" )
                                        }
                                    }
                                }
                                break
                            case "RPS": // Redundant Power System
                                PostStateToChild( "${ Device }", "12v Power Remaining", it.value.power_remaining_12v )
                                PostStateToChild( "${ Device }", "54v Power Remaining", it.value.power_remaining_54v )
                                PostStateToChild( "${ Device }", "12v Power Supply", it.value.power_supply_12v )
                                PostStateToChild( "${ Device }", "54v Power Supply", it.value.power_supply_54v )
                                PostEventToChild( "${ Device }", "12v Power Delivering", it.value.power_delivering_12v )
                                PostEventToChild( "${ Device }", "54v Power Delivering", it.value.power_delivering_54v )
                                PostEventToChild( "${ Device }", "12v Power Usage", ( Math.round( ( ( getChildDevice( "${ Device }" ).ReturnState( '12v Power Supply' ) - getChildDevice( "${ Device }" ).ReturnState( '12v Power Remaining' ) ) / getChildDevice( "${ Device }" ).ReturnState( '12v Power Supply' ) ) * 100 ) / 100 ), "%" )
                                PostEventToChild( "${ Device }", "54v Power Usage", ( Math.round( ( ( getChildDevice( "${ Device }" ).ReturnState( '54v Power Supply' ) - getChildDevice( "${ Device }" ).ReturnState( '54v Power Remaining' ) ) / getChildDevice( "${ Device }" ).ReturnState( '54v Power Supply' ) ) * 100 ) / 100 ), "%" )
                                it.value.rps_port_table.each(){
                                    def PortNum = "0"
                                    if( it.port_idx < 10 ){
                                        PortNum = "0${ it.port_idx }"
                                    } else {
                                        PortNum = "${ it.port_idx }"
                                    }
                                    def TempMap = [ PortID: PortNum, Mode: it.port_mode ]
                                    PostEventToChild( "${ Device }", "RPS Port ${ PortNum } Status",  TempMap )
                                }
                                break
                            default:
                                def TempMap = [ PortID: it.value.rps_port_table[ 0 ].port_idx, Mode: it.value.rps_port_table[ 0 ].port_mode ]
                                PostEventToChild( "${ Device }", "RPS Port",  TempMap )
                                break
                        }
                    }
                    break
                case "serial":
                    PostStateToChild( "${ Device }", "Serial", it.value )
                    break
                case "rps_override":
                    if( it.value.rps_port_table.size() > 0 ){
                        it.value.rps_port_table.each(){
                            def PortNum = "0"
                            if( it.port_idx < 10 ){
                                PortNum = "0${ it.port_idx }"
                            } else {
                                PortNum = "${ it.port_idx }"
                            }
                            def TempMap = [ PortID: PortNum, Mode: it.port_mode ]
                            PostStateToChild( "${ Device }", "RPS Port ${ PortNum } Status",  TempMap )
                        }
                    }
                    break
                case "device_id":
                    PostStateToChild( "${ Device }", "ID", it.value )
                    break
                case "type":
                    PostStateToChild( "${ Device }", "Hardware Type", it.value )
                    break
                case "hardwareRevision":
                    PostStateToChild( "${ Device }", "Hardware Revision", it.value )
                    break
                case "firmwareVersion":
                    PostStateToChild( "${ Device }", "Firmware Version", it.value )
                    break
                case "state":
                    PostEventToChild( "${ Device }", "Device Status", it.value )
                    break
                case "isConnected":
                    if( it.value ){
                        PostEventToChild( "${ Device }", "presence", "present" )
                    } else {
                        PostEventToChild( "${ Device }", "presence", "not present" )
                    }
                    break
                case "latestFirmwareVersion":
                    PostStateToChild( "${ Device }", "Latest Firmware Version", it.value )
                    break
                case "mac":
                    PostStateToChild( "${ Device }", "MAC", it.value )
                    break
                case "uptime":
                    def TempUptime = it.value as int
                    def TempUptimeDays = Math.round( TempUptime / 86400 )
                    def TempUptimeHours = Math.round( ( TempUptime % 86400 ) / 3600 )
                    def TempUptimeMinutes = Math.round( ( TempUptime % 3600 ) / 60 )
                    def TempUptimeString = "${ TempUptimeDays } Day"
                    if( TempUptimeDays != 1 ){
                        TempUptimeString += "s"
                    }
                    TempUptimeString += " ${ TempUptimeHours } Hour"
                    if( TempUptimeHours != 1 ){
                        TempUptimeString += "s"
                    }
                    TempUptimeString += " ${ TempUptimeMinutes } Minute"
                    if( TempUptimeMinutes != 1 ){
                        TempUptimeString += "s"
                    }
                    PostEventToChild( "${ Device }", "Uptime", TempUptimeString )
                    break
                case "last_seen":
                    PostEventToChild( "${ Device }", "LastSeen", ConvertEpochToDate( "${ it.value }" ) )
                    break
                case "has_speaker":
                    PostStateToChild( "${ Device }", "Has Speaker", it.value )
                    break
                case "led_override":
                    PostEventToChild( "${ Device }", "LED Override", it.value )
                    break
                case "led_override_color":
                    PostEventToChild( "${ Device }", "LED Color", it.value )
                    break
                case "led_override_color_brightness":
                    PostEventToChild( "${ Device }", "LED Brightness", it.value )
                    break
                case "temperatures":
                    it.value.each(){
                        if( it.name == "CPU" ){
                            PostEventToChild( "${ Device }", "temperature", ConvertTemperature( "C", it.value ), "°${ location.getTemperatureScale() }" )
                            PostEventToChild( "${ Device }", "CPU Temperature", ConvertTemperature( "C", it.value ), "°${ location.getTemperatureScale() }" )
                        } else if( it.name == "Local" ){
                            PostEventToChild( "${ Device }", "Local Temperature", ConvertTemperature( "C", it.value ), "°${ location.getTemperatureScale() }" )
                        } else if( it.name == "PHY" ){
                            PostEventToChild( "${ Device }", "PHY Temperature", ConvertTemperature( "C", it.value ), "°${ location.getTemperatureScale() }" )
                        }
                    }
                    break
                case "led_state":
                    if( it.value.pattern != null ){
                        PostStateToChild( "${ Device }", "LED Pattern", it.value.pattern )
                    }
                    if( it.value.tempo != null ){
                        PostStateToChild( "${ Device }", "LED Tempo", it.value.tempo )
                    }
                    break
                case "speedtest-status":
                    if( it.value.xput_download != null ){
                        PostEventToChild( "${ Device }", "Speedtest Download", ( Math.round( ( it.value.xput_download * 100 ) ) / 100 ) )
                    }
                    if( it.value.xput_upload != null ){
                        PostEventToChild( "${ Device }", "Speedtest Upload", ( Math.round( ( it.value.xput_upload * 100 ) ) / 100 ) )
                    }
                    break
                case "uptime_stats":
                    if( it.value.WAN != null ){
                        if( it.value.WAN.availability != null ){
                            PostEventToChild( "${ Device }", "WAN1 Average Latency", it.value.WAN.latency_average )
                            PostEventToChild( "${ Device }", "WAN1 Availability", ( Math.round( ( it.value.WAN.availability * 100 ) ) / 100 ) )
                        }
                    }
                    if( it.value.WAN2 != null ){
                        if( it.value.WAN2.availability != null ){
                            PostEventToChild( "${ Device }", "WAN2 Average Latency", it.value.WAN2.latency_average )
                            PostEventToChild( "${ Device }", "WAN2 Availability", ( Math.round( ( it.value.WAN2.availability * 100 ) ) / 100 ) )
                        }
                    }
                    break
                case "lan-num_sta":
                    PostStateToChild( "${ Device }", "Total Clients", it.value )
                    break
                case "user-lan-num_sta":
                    PostStateToChild( "${ Device }", "User Clients", it.value )
                    break
                case "guest-lan-num_sta":
                    PostStateToChild( "${ Device }", "Guest Clients", it.value )
                    break
                case "storage": // Doing nothing with it at this time
                    
                    break
                case "outlet_ac_power_budget":
                    PostStateToChild( "${ Device }", "AC Power Budget", it.value )
                    break
                case "outlet_ac_power_consumption":
                    PostEventToChild( "${ Device }", "power", it.value, "W" )
                    PostEventToChild( "${ Device }", "AC Power Consumption", it.value, "W" )
                    break
                case "outlet_table":
                    if( getChildDevice( "${ Device }" ) == null ){
                        break    
                    }
                    switch( getChildDevice( "${ Device }" ).ReturnState( "Model" ) ){
                        case "UP1":
                            // Do nothing with the UP1
                            break
                        case "UP6":
                            it.value.each(){
                                if( it.index != null && it.relay_state != null ){
                                    if( it.index != 7 ){
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                        }
                                    } else {
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "USB Ports", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "USB Ports", "off" )
                                        }
                                    }
                                }
                            }
                            break
                        case "USPPDUP":
                            def TotalCurrent = 0
                            def Voltage = 0
                            it.value.each(){
                                def TempMap = []
                                if( it.index != null ){
                                    if( it.index > 4 ){
                                        TempMap = [ Index: it.index, Relay_State: "unknown", Cycle_Enabled: "unknown", Name: "Outlet ${ it.index }", Voltage: "unknown", Current: "unknown", Power: "unknown", Power_Factor: "unknown" ]
                                        if( it.relay_state != null ){
                                            if( it.relay_state ){
                                                PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                                TempMap.Relay_State = "true"
                                            } else {
                                                PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                                TempMap.Relay_State = "false"
                                            }
                                        }
                                        if( it.cycle_enabled != null ){
                                            if( it.cycle_enabled ){
                                                TempMap.Cycle_Enabled = "true"
                                            } else {
                                                TempMap.Cycle_Enabled = "false"
                                            }
                                        }
                                        if( it.name != null ){
                                            TempMap.Name = "${ it.name }"
                                        }
                                        if( it.outlet_voltage != null ){
                                            TempMap.Voltage = it.outlet_voltage as float
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index } Voltage", it.outlet_voltage as float, "V" )
                                            Voltage = it.outlet_voltage as float
                                        }
                                        if( it.outlet_current != null ){
                                            TempMap.Current = it.outlet_current as float
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index } Current", it.outlet_current as float, "A" )
                                            TotalCurrent = ( TotalCurrent + ( it.outlet_current as float ) )
                                        }
                                        if( it.outlet_power != null ){
                                            TempMap.Power = it.outlet_power as float
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index } Power", it.outlet_power as float, "W" )
                                        }
                                        if( it.outlet_power_factor != null ){
                                            TempMap.Power_Factor = it.outlet_power_factor as float
                                        }
                                        PostEventToChild( "${ Device }", "Outlet ${ it.index } Status", TempMap )
                                    } else {
                                        TempMap = [ Index: it.index, Relay_State: "unknown", Cycle_Enabled: "unknown", Name: "USB Port ${ x }" ]
                                        if( it.relay_state != null ){
                                            if( it.relay_state ){
                                                PostEventToChild( "${ Device }", "USB Port ${ it.index }", "on" )
                                                TempMap.Relay_State = "true"
                                            } else {
                                                PostEventToChild( "${ Device }", "USB Port ${ it.index }", "off" )
                                                TempMap.Relay_State = "false"
                                            }
                                        }
                                        if( it.cycle_enabled != null ){
                                            if( it.cycle_enabled ){
                                                TempMap.Cycle_Enabled = "true"
                                            } else {
                                                TempMap.Cycle_Enabled = "false"
                                            }
                                        }
                                        if( it.name != null ){
                                            TempMap.Name = "${ it.name }"
                                        }
                                        PostEventToChild( "${ Device }", "USB Port ${ it.index } Status", TempMap )
                                    }
                                }
                            }
                            PostEventToChild( "${ Device }", "voltage", Voltage, "V" )
                            PostEventToChild( "${ Device }", "current", TotalCurrent, "A" )
                            break
                        default:
                            it.value.each(){
                                if( it.index != null && it.relay_state != null ){
                                    if( it.relay_state ){
                                        PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                    } else {
                                        PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                    }
                                }
                            }
                            break
                    }
                    break
                case "outlet_enabled":
                    if( getChildDevice( "${ Device }" ) == null ){
                        break    
                    }
                    switch( getChildDevice( "${ Device }" ).ReturnState( "Model" ) ){
                        case "USPPDUP":
                            if( it.value ){
                                PostEventToChild( "${ Device }", "Outlet Enabled", "true" )
                            } else {
                                PostEventToChild( "${ Device }", "Outlet Enabled", "false" )
                            }
                            break
                        case "UP6":
                            if( it.value ){
                                PostEventToChild( "${ Device }", "Outlet Enabled", "true" )
                            } else {
                                PostEventToChild( "${ Device }", "Outlet Enabled", "false" )
                            }
                            break
                        case "UP1":
                            if( it.value ){
                                PostEventToChild( "${ Device }", "Outlet Enabled", "true" )
                            } else {
                                PostEventToChild( "${ Device }", "Outlet Enabled", "false" )
                            }
                            break
                        default:
                            Logging( "Outlet Enabled for ${ Device } = ${ it.value }", 4 )
                            break
                    }
                    break
                case "outlet_overrides":
                    if( getChildDevice( "${ Device }" ) == null ){
                        break    
                    }
                    if( it.value.size() > 0 ){
                        PostStateToChild( "${ Device }", "Outlet Overrides",  it.value )
                    }
                    switch( getChildDevice( "${ Device }" ).ReturnState( "Model" ) ){
                        case "UP1":
                            if( it.value.relay_state ){
                                PostEventToChild( "${ Device }", "switch", "on" )
                            } else {
                                PostEventToChild( "${ Device }", "switch", "off" )
                            }
                            break
                        case "UP6":
                            it.value.each(){
                                if( it.index != null && it.relay_state != null ){
                                    if( it.index != 7 ){
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                        }
                                    } else {
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "USB Ports", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "USB Ports", "off" )
                                        }
                                    }
                                }
                            }
                            break
                        case "USPPDUP":
                            it.value.each(){
                                if( it.index != null && it.relay_state != null ){
                                    if( it.index > 4 ){
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                        }
                                    } else {
                                        if( it.relay_state ){
                                            PostEventToChild( "${ Device }", "USB Port ${ it.index }", "on" )
                                        } else {
                                            PostEventToChild( "${ Device }", "USB Port ${ it.index }", "off" )
                                        }
                                    }
                                }
                            }
                            break
                        default:
                            it.value.each(){
                                if( it.index != null && it.relay_state != null ){
                                    if( it.relay_state ){
                                        PostEventToChild( "${ Device }", "Outlet ${ it.index }", "on" )
                                    } else {
                                        PostEventToChild( "${ Device }", "Outlet ${ it.index }", "off" )
                                    }
                                }
                            }
                            break
                    }
                    break
                case "port_overrides":
                    if( it.value.size() > 0 ){
                        PostStateToChild( "${ Device }", "Port Overrides",  it.value )
                    }
                case "port_table":
                    if( it.value.size() > 0 ){
                        if( getChildDevice( "${ Device }" ) == null ){
                            break    
                        }
                        switch( getChildDevice( "${ Device }" ).ReturnState( "Device Type" ) ){
                            case "BasicAP": // APs without Status LED Ring
                            case "AP": // APs with Status LED Ring
                            case "UP1": // Plug
                            case "Plug": // Plug
                            case "UP6": // Powerstrip
                            case "USPPDUP": // SmartPower Pro PDU
                            case "RPS": // Redundant Power System
                                break
                            case "USW48":
                            case "USW24":
                            case "USW16":
                            case "USW8":
                            case "UXG":
                            case "UXGB":
                            case "USG":
                            case "UGW3":
                            case "UGW4":
                            case "UX":
                            case "UDM": // Unifi Dream Machine
                            case "UDMP": // Unifi Dream Machine Pro
                            case "UDMPSE": // Unifi Dream Machine Pro SE
                            case "USMINI": // Flex Mini 5 Port Switch
                            case "USF5P": // Flex 5 Port PoE Switch
                            case "UHDIW": // In-Wall AP with PoE Port
                            //case "U6IW": // U6 In Wall
                            //case "U6ENT": // U6 Enterprise In Wall
                            case "ACIW":
                            case "USW24PoE":
                            case "USW16PoE":
                            case "USW16LPoE":
                            case "USW8PoE":
                            case "USW8PoE60":
                            case "USW8LPoE":
                            case "USW48PoE":
                            case "USAGGPRO": // Aggregate Pro
                            default:
                                it.value.each(){
                                    if( it.port_idx != null ){
                                        def PortNum = "0"
                                        if( it.port_idx < 10 ){
                                            PortNum = "0${ it.port_idx }"
                                        } else {
                                            PortNum = "${ it.port_idx }"
                                        }
                                        def TempMap = getChildDevice( "${ Device }" ).ReturnState( "Port ${ PortNum } Status" )
                                        if( TempMap == null ){
                                            TempMap = [ PortID: PortNum, Enabled: "unknown", Media: "unknown", PoE: "unknown", PoE_Status: "unknown", PoE_Usage: "unknown", Speed: 0, Connected: "unknown" ]
                                        }
                                        if( it.enable != null ){
                                            if( it.enable ){
                                                TempMap.Enabled = "true"
                                            } else {
                                                TempMap.Enabled = "false"
                                            }
                                        }
                                        if( it.port_poe != null ){
                                            if( it.port_poe ){
                                                TempMap.PoE = "true"
                                                TempMap.PoE_Status = it.poe_mode
                                                TempMap.PoE_Usage = it.poe_power
                                            } else {
                                                TempMap.PoE = "false"
                                                TempMap.PoE_Status = "NA"
                                                TempMap.PoE_Usage = "NA"
                                            }
                                        }
                                        if( it.media != null ){
                                            TempMap.Media = it.media
                                        }
                                        if( it.speed != null ){
                                            TempMap.Speed = it.speed
                                        }
                                        if( it.up != null ){
                                            if( it.up ){
                                                TempMap.Connected = "true"
                                            } else {
                                                TempMap.Connected = "false"
                                            }
                                        }
                                        if( it.native_networkconf_id != null ){
                                            TempMap.NetworkConfigID = it.native_networkconf_id
                                        }
                                        PostEventToChild( "${ Device }", "Port ${ PortNum } Status",  TempMap )
                                    }
                                }
                                break
                        }
                    }
                    break
                case "total_used_power":
                    PostEventToChild( "${ Device }", "power", it.value, "W" )
                    break
                case "uplink":
                    if( it.value.port_idx != null ){
                        def PortNum = "0"
                        if( it.value.port_idx < 10 ){
                            PortNum = "0${ it.value.port_idx }"
                        } else {
                            PortNum = "${ it.value.port_idx }"
                        }
                        def TempMap = [ PortID: PortNum, Enabled: "unknown", Media: "unknown", Speed: 0, RemotePort: "unknown", UplinkDevice: "unknown" ]
                        if( it.value.enable ){
                            TempMap.Enabled = "true"
                        } else {
                            TempMap.Enabled = "false"
                        }
                        TempMap.Media = it.value.media
                        TempMap.Speed = it.value.speed
                        TempMap.RemotePort = it.value.uplink_remote_port
                        TempMap.UplinkDevice = it.value.uplink_device_name
                        PostStateToChild( "${ Device }", "Uplink",  TempMap )
                    }  
                    break
                case "model_eol_date":
                
                    break
                case "power_source_voltage":
                    PostEventToChild( "${ Device }", "voltage", it.value, "v" )
                    break
                case "last_scan":
                    def TempScanTime = it.value as int
                    def TempScanTimeDays = Math.round( TempScanTime / 86400 )
                    def TempScanTimeHours = Math.round( ( TempScanTime % 86400 ) / 3600 )
                    def TempScanTimeMinutes = Math.round( ( TempScanTime % 3600 ) / 60 )
                    def TempScanTimeString = "${ TempScanTimeDays } Day"
                    if( TempScanTimeDays != 1 ){
                        TempScanTimeString += "s"
                    }
                    TempScanTimeString += " ${ TempScanTimeHours } Hour"
                    if( TempScanTimeHours != 1 ){
                        TempScanTimeString += "s"
                    }
                    TempScanTimeString += " ${ TempScanTimeMinutes } Minute"
                    if( TempScanTimeMinutes != 1 ){
                        vString += "s"
                    }
                    PostStateToChild( "${ Device }", "Last Scan", TempScanTimeString )
                    break
                case "is_access_point":
                    if( it.value ){
                        PostStateToChild( "${ Device }", "Access Point", "true" )
                    } else {
                        PostStateToChild( "${ Device }", "Access Point", "false" )
                    }
                    break
                case "support_wifi6e":
                    if( it.value ){
                        PostStateToChild( "${ Device }", "SupportWiFi6e", "true" )
                    } else {
                        PostStateToChild( "${ Device }", "SupportWiFi6e", "false" )
                    }
                    break
                case "last_wan_ip":
                    //PostStateToChild( "${ Device }", "Last WAN IP", it.value )
                    PostEventToChild( "${ Device }", "Last_WAN_IP", it.value )
                    break
                case "ether_lighting":
                    def TempMap = [ Mode: "unknown", Brightness: 100, Behavior: "unknown" ]
                    if( it.value.mode != null ){
                        TempMap.Mode = it.value.mode
                    }
                    if( it.value.brightness != null ){
                        TempMap.Brightness = it.value.brightness
                    }
                    if( it.value.behavior != null ){
                        TempMap.Behavior = it.value.behavior
                    }
                    PostStateToChild( "${ Device }", "Etherlighting",  TempMap )
                    break
                case "shortname":
                    PostStateToChild( "${ Device }", "Shortname",  it.value )
                    break
                case "sysid":
                    PostStateToChild( "${ Device }", "SysID",  it.value )
                    break
                case "active_geo_info":
                    PostStateToChild( "${ Device }", "GeoInfo",  it.value )
                    break
                case "ipv6":
                    PostStateToChild( "${ Device }", "IPv6",  it.value )
                    break
                case "last_wan_ips":
                    PostStateToChild( "${ Device }", "LastWANIP",  it.value )
                    break
                case "hardware_uuid":
                    PostStateToChild( "${ Device }", "HardwareUUID",  it.value )
                    break
                case "service_mac":
                    PostStateToChild( "${ Device }", "ServiceMAC",  it.value )
                    break
                case "last_wan_interfaces":
                    PostStateToChild( "${ Device }", "LastWANInterfaces",  it.value )
                    break
                case "upgrade_to_firmware":
                    // ignoring for now because need to determine if this is something to notify users of or not
                    break
                // Ignored values or ones processed otherwised
                case "fixed_ap_available":
                case "udapi_caps":
                case "teleport_version":
                case "lcm_tracker_seed":
                case "lcm_tracker_enabled":
                case "element_ap_serial":
                case "required_version":
                case "displayable_version":
                case "sys_stats":
                case "adopt_url":
                case "dhcp_server_table":
                case "hw_caps":
                case "config_network":    
                case "adopt_ip":
                case "num_sta":
                case "unsupported_reason":
                case "anon_id":
                case "connect_request_port":
                case "site_id":
                case "rollupgrade":
                case "mgmt_network_id":
                case "connected_at":
                case "start_disconnected_millis":
                case "provisioned_at":
                case "inform_url":
                case "ethernet_table":
                case "default":
                case "user-num_sta":
                case "last_uplink":
                case "tx_bytes":
                case "architecture":
                case "x_aes_gcm":
                case "stat":
                case "next_heartbeat_at":
                case "model_incompatible":
                case "downlink_table":
                case "x_ssh_hostkey_fingerprint":
                case "has_temperature":
                case "_uptime":
                case "start_connected_millis":
                case "power_source_ctrl_enabled":
                case "adopted":
                case "hash_id":
                case "connect_request_ip":
                case "prev_non_busy_state":
                case "guest-num_sta":
                case "setup_id":
                case "snmp_contact":
                case "upgradable":
                case "adoptable_when_upgraded":
                case "syslog_key":
                case "manufacturer_id":
                case "jumboframe_enabled":
                case "ssh_session_table":
                case "stp_version":
                case "considered_lost_at":
                case "fw_caps":
                case "_id":
                case "internet":
                case "locating":
                case "gateway_mac":
                case "stp_priority":
                case "x_fingerprint":
                case "two_phase_adopt":
                case "inform_ip":
                case "cfgversion":
                case "known_cfgversion":
                case "next_interval":
                case "flowctrl_enabled":
                case "sys_error_caps":
                case "unsupported":
                case "dot1x_portctrl_enabled":
                case "rx_bytes":
                case "x_has_ssh_hostkey":
                case "x_authkey":
                case "lldp_table":
                case "switch_caps":
                case "snmp_location":
                case "discovered_via":
                case "bytes":
                case "vap_table":
                case "rx_bytes-d":
                case "guest_token":
                case "vwire_vap_table":
                case "guest-wlan-num_sta":
                case "scan_radio_table":
                case "x_vwirekey":
                case "vwireEnabled":
                case "element_peer_mac":
                case "scanning":
                case "spectrum_scanning":
                case "isolated":
                case "acc_meter_stats":
                case "has_eth1":
                case "port_stats":
                case "hide_ch_width":
                case "bytes-d":
                case "country_code":
                case "countrycode_table":
                case "antenna_table":
                case "wifi_caps":
                case "user-wlan-num_sta":
                case "bytes-r":
                case "radio_table":
                case "uplink_table":
                case "radio_table_stats":
                case "tx_bytes-d":
                case "vwire_table":
                case "meshv3_peer_mac":
                case "guest_kicks":
                case "setup_provision_completed":
                case "ethernet_overrides":
                case "num_mobile":
                case "num_handheld":
                case "bandsteering_mode":
                case "num_desktop":
                case "wlangroup_id_ng":
                case "atf_enabled":
                case "ruleset_interfaces":
                case "usg_caps":
                case "x_inform_authkey":
                case "speedtest-status-saved":
                case "network_table":
                case "wlan-num_sta":
                case "geo_info":
                case "config_network_lan":
                case "unifi_care":
                case "setup_provision_tracking":
                case "wan1":
                case "wan2":
                case "supports_fingerprint_ml":
                case "wlangroup_id_na":
                case "x_ssh_hostkey":
                case "min_inform_interval_seconds":
                case "connection_network_name":
                case "startup_timestamp":
                case "lcm_night_mode_enabled":
                case "element_uplink_ap_mac":
                case "uplink_bssid":
                case "uplink_depth":
                case "disconnection_reason":
                case "lan_ip":
                case "disabled":
                case "mesh_uplink_1":
                case "mesh_uplink_2":
                case "adopt_tries":
                case "adopt_state":
                case "x_adopt_username":
                case "x_adopt_password":
                case "mesh_sta_vap_enabled":
                case "safe_for_autoupgrade":
                case "adoption_completed":
                case "ble_caps":
                case "disconnected_at":
                case "adopted_by_client":
                case "adopted_at":
                case "downlink_lldp_macs":
                case "upgrade_state":
                case "adopt_status":
                case "adopt_manual":
                case "netmask":
                case "in_gateway_mode":
                case "detailed_states":
                case "upgrade_duration":
                case "last_connection_network_name":
                case "fw2_caps":
                case "reboot_duration":
                case "udapi_version":
                case "usg2_caps":
                case "reported_networks":
                case "credential_caps":
                case "ipv4_active_leases":
                case "last_config_applied_successfully":
                case "has_lcm_override":
                case "ipv4_lease_expiration_timestamp_seconds":
                case "connection_network_id":
                case "last_connection_network_id":
                case "slimcfg_caps":
                case "switch_vlan_enabled":
                case "dns_shield_server_list_hash":
                case "wifi_caps2":
                case "dhcp_excluded_ip_list":
                case "ids_ips_last_known_signature":
                case "supported_afc_regions":
                case "lcm_orientation_override":
                case "ids_ips_signature":
                case "external_id":
                    break
                default:
                    Logging( "Unhandled data for ${ Device } ${ it.key } = ${ it.value }", 3 )
                    break
            }
        }
    }
}

// Pings a specific IP address using the Hubitat not the Unifi
def Ping( IP ){
    def TempVersion
    if( location.hub.firmwareVersionString != null ){
        TempVersion = location.hub.firmwareVersionString
        TempVersion = TempVersion.split( /\./ )
        if( TempVersion[ 0 ] >= 2 && TempVersion[ 1 ] >= 2 && TempVersion[ 2 ] >= 7 ){
            ProcessEvent( "Ping Result", "Ping for ${ IP }: ${ hubitat.helper.NetworkUtils.ping( IP ) }" )
        } else {
            Logging( "Ping feature requires Hubitat hub version of 2.2.7 or greater.", 2 )
        }
    } else {
        Logging( "Ping feature requires Hubitat hub version of 2.2.7 or greater.", 2 )
    }
}

// installed is called when the device is installed
def installed(){
	Logging( "Installed", 2 )
}

// initialize is called when the device is initialized
def initialize(){
	Logging( "Initialized", 2 )
}

// uninstalling device so make sure to clean up children
void uninstalled() {
    // Delete all children
    getChildDevices().each{
        deleteChildDevice( it.deviceNetworkId )
    }
    unschedule()
    Logging( "Uninstalled", 2 )
}

// parse appears to be one of those "special" methods for when data is returned 
def parse( String description ){
    Logging( "Parse = ${ description }", 3 )
}

// Used to convert epoch values to text dates
def String ConvertEpochToDate( String Epoch ){
    Long Temp = Epoch.toLong()
    def date
    if( Temp <= 9999999999 ){
        date = new Date( ( Temp * 1000 ) ).toString()
    } else {
        date = new Date( Temp ).toString()
    }
    return date
}

// Checks the location.getTemperatureScale() to convert temperature values
def ConvertTemperature( String Scale, Number Value ){
    if( Value != null ){
        def ReturnValue = Value as double
        if( location.getTemperatureScale() == "C" && Scale.toUpperCase() == "F" ){
            ReturnValue = ( ( ( Value - 32 ) * 5 ) / 9 )
            Logging( "Temperature Conversion ${ Value }°F to ${ ReturnValue }°C", 4 )
        } else if( location.getTemperatureScale() == "F" && Scale.toUpperCase() == "C" ) {
            ReturnValue = ( ( ( Value * 9 ) / 5 ) + 32 )
            Logging( "Temperature Conversion ${ Value }°C to ${ ReturnValue }°F", 4 )
        } else if( ( location.getTemperatureScale() == "C" && Scale.toUpperCase() == "C" ) || ( location.getTemperatureScale() == "F" && Scale.toUpperCase() == "F" ) ){
            ReturnValue = Value
        }
        def TempInt = ( ReturnValue * 100 ) as int
        ReturnValue = ( TempInt / 100 )
        return ReturnValue
    }
}

// Process data to check against current state value and then send an event if it has changed
def ProcessEvent( Variable, Value, Unit = null, ForceEvent = false ){
    if( ( state."${ Variable }" != Value ) || ( ForceEvent == true ) ){
        state."${ Variable }" = Value
        if( Unit != null ){
            Logging( "Event: ${ Variable } = ${ Value }${ Unit }", 4 )
            sendEvent( name: "${ Variable }", value: Value, unit: Unit, isStateChange: true )
        } else {
            Logging( "Event: ${ Variable } = ${ Value }", 4 )
            sendEvent( name: "${ Variable }", value: Value, isStateChange: true )
        }
       //UpdateTile( "${ Value }" )
    }
}

// Process data to check against current state value
def ProcessState( Variable, Value ){
    if( state."${ Variable }" != Value ){
        Logging( "State: ${ Variable } = ${ Value }", 4 )
        state."${ Variable }" = Value
        //UpdateTile( "${ Value }" )
    }
}

// Handle child types in one place rather than repeating
def GetChildType( Value ){
    def ChildType = "Generic"
    switch( TempChild[ 0 ] ){
        case "Presence":
            ChildType = "Presence"
            break
        case "UP6":
            ChildType = "UP6"
            break
        case "USPPDUP":
            ChildType = "USPPDUP"
            break
        case "USMINI":
            ChildType = "USMINI"
            break
        case "USF5P":
            ChildType = "USF5P"
            break
        case "USW48PoE":
            ChildType = "USW48PoE"
            break
        case "USW24PoE":
            ChildType = "USW24PoE"
            break
        case "USW16PoE":
            ChildType = "USW16PoE"
            break
        case "USW8PoE":
            ChildType = "USW8PoE"
            break
        case "USW16LPoE":
            ChildType = "USW16LPoE"
            break
        case "USW8PoE60":
            ChildType = "USW8PoE60"
            break
        case "USW8LPoE":
            ChildType = "USW8LPoE"
            break
        case "USW48":
            ChildType = "USW48"
            break
        case "USW24":
            ChildType = "USW24"
            break
        case "USW16":
            ChildType = "USW16"
            break
        case "USW8":
            ChildType = "USW8"
            break
        case "US8":
            ChildType = "US8"
            break
        case "ACIW":
            ChildType = "ACIW"
            break
        case "UHDIW":
            ChildType = "UHDIW"
            break
        case "BasicAP":
            ChildType = "BasicAP"
            break
        case "AP":
            ChildType = "AP"
            break
        case "RPS":
            ChildType = "RPS"
            break
        case "UDM":
            ChildType = "UDM"
            break
        case "UDMP":
            ChildType = "UDMP"
            break
        case "UDMPSE":
            ChildType = "UDMPSE"
            break
        case "UDR":
            ChildType = "UDR"
            break
        case "UP1":
        case "Plug":
            ChildType = "Plug"
            break
        case "UGW3":
            ChildType = "UGW3"
            break
        case "UGW4":
            ChildType = "UGW4"
            break
        case "USG":
            ChildType = "USG"
            break
        case "UX":
            ChildType = "UX"
            break
        case "UXG":
            ChildType = "UXG"
            break
        case "UXGB":
            ChildType = "UXGB"
            break
        case "UCGMax":
            ChildType = "UCGMax"
            break
        case "USAGGPRO":
            ChildType = "USAGGPRO"
            break
        case "USPM16":
            ChildType = "USPM16"
            break
        case "USPM16P":
            ChildType = "USPM16P"
            break
        case "USPM24":
            ChildType = "USPM24"
            break
        case "USPM24P":
            ChildType = "USPM24P"
            break
        case "USPM48":
            ChildType = "USPM48"
            break
        case "USPM48P":
            ChildType = "USPM48P"
            break
        default:
            ChildType = "Generic"
            break
    }
    return ChildType
}
    
// Post data to child device
def PostEventToChild( Child, Variable, Value, Unit = null, ForceEvent = null ){
    if( "${ Child }" != null ){
        if( getChildDevice( "${ Child }" ) == null ){
            TempChild = Child.split( " " )
            addChild( "${ Child }", GetChildType( TempChild[ 0 ] ) )
        }
        if( getChildDevice( "${ Child }" ) != null ){
            if( Unit != null ){
                if( ForceEvent != null ){
                    getChildDevice( "${ Child }" ).ProcessEvent( "${ Variable }", Value, "${ Unit }", ForceEvent )
                    Logging( "Child Event: ${ Variable } = ${ Value }${ Unit }", 4 )
                } else {
                    getChildDevice( "${ Child }" ).ProcessEvent( "${ Variable }", Value, "${ Unit }" )
                    Logging( "Child Event: ${ Variable } = ${ Value }", 4 )
                }
            } else {
                if( ForceEvent != null ){
                    getChildDevice( "${ Child }" ).ProcessEvent( "${ Variable }", Value, null, ForceEvent )
                    Logging( "Child Event: ${ Variable } = ${ Value }${ Unit }", 4 )
                } else {
                    getChildDevice( "${ Child }" ).ProcessEvent( "${ Variable }", Value )
                    Logging( "Child Event: ${ Variable } = ${ Value }", 4 )
                }
            }
        } else {
            if( Unit != null ){
                Logging( "Failure to add ${ Child } and post ${ Variable }=${ Value }${ Unit }", 5 )
            } else {
                Logging( "Failure to add ${ Child } and post ${ Variable }=${ Value }", 5 )
            }
        }
    } else {
        Logging( "Failure to add child because child name was null", 5 )
    }
}

// Post data to child device
def PostStateToChild( Child, Variable, Value ){
    if( "${ Child }" != null ){
        if( getChildDevice( "${ Child }" ) == null ){
            TempChild = Child.split( " " )
            addChild( "${ Child }", GetChildType( TempChild[ 0 ] ) )
        }
        if( getChildDevice( "${ Child }" ) != null ){
            //Logging( "${ Child } State: ${ Variable } = ${ Value }", 4 )
            getChildDevice( "${ Child }" ).ProcessState( "${ Variable }", Value )
        } else {
            Logging( "Failure to add ${ Child } and post ${ Variable }=${ Value }", 5 )
        }
    } else {
        Logging( "Failure to add child because child name was null", 5 )
    }
}

// Adds a UnifiNetworkChild child device
// Based on @mircolino's method for child sensors
def addChild( String DNI, String ChildType ){
    try{
        Logging( "addChild(${ DNI })", 3 )
        if( UnifiChildren ){
            switch( ChildType ){
                case "Presence":
                    addChildDevice( "UnifiNetworkChild-Presence", DNI, [ name: "${ DNI }" ] )
                    break
                case "ClientCheck":
                    addChildDevice( "UnifiNetworkChild-ClientCheck", DNI, [ name: "${ DNI }" ] )
                    break
                case "UP6":
                    addChildDevice( "UnifiNetworkChild-UP6", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPPDUP":
                    addChildDevice( "UnifiNetworkChild-USPPDUP", DNI, [ name: "${ DNI }" ] )
                    break
                case "USMINI":
                    addChildDevice( "UnifiNetworkChild-USMINI", DNI, [ name: "${ DNI }" ] )
                    break
                case "USF5P":
                    addChildDevice( "UnifiNetworkChild-USF5P", DNI, [ name: "${ DNI }" ] )
                    break
                case "ACIW":
                    addChildDevice( "UnifiNetworkChild-ACIW", DNI, [ name: "${ DNI }" ] )
                    break
                case "UHDIW":
                    addChildDevice( "UnifiNetworkChild-UHDIW", DNI, [ name: "${ DNI }" ] )
                    break
                case "BasicAP":
                    addChildDevice( "UnifiNetworkChild-BasicAP", DNI, [ name: "${ DNI }" ] )
                    break
                case "AP":
                    addChildDevice( "UnifiNetworkChild-AP", DNI, [ name: "${ DNI }" ] )
                    break
                case "RPS":
                    addChildDevice( "UnifiNetworkChild-RPS", DNI, [ name: "${ DNI }" ] )
                    break
                case "UDM":
                    addChildDevice( "UnifiNetworkChild-UDM", DNI, [ name: "${ DNI }" ] )
                    break
                case "UDMP":
                    addChildDevice( "UnifiNetworkChild-UDMP", DNI, [ name: "${ DNI }" ] )
                    break
                case "UDMPSE":
                    addChildDevice( "UnifiNetworkChild-UDMPSE", DNI, [ name: "${ DNI }" ] )
                    break
                case "UDR":
                    addChildDevice( "UnifiNetworkChild-UDR", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW48PoE":
                    addChildDevice( "UnifiNetworkChild-USW48PoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW48":
                    addChildDevice( "UnifiNetworkChild-USW48", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW24PoE":
                    addChildDevice( "UnifiNetworkChild-USW24PoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW24":
                    addChildDevice( "UnifiNetworkChild-USW24", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW16PoE":
                    addChildDevice( "UnifiNetworkChild-USW16PoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW16LPoE":
                    addChildDevice( "UnifiNetworkChild-USW16LPoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW16":
                    addChildDevice( "UnifiNetworkChild-USW16", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW8PoE":
                    addChildDevice( "UnifiNetworkChild-USW8PoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW8PoE60":
                    addChildDevice( "UnifiNetworkChild-USW8PoE60", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW8LPoE":
                    addChildDevice( "UnifiNetworkChild-USW8LPoE", DNI, [ name: "${ DNI }" ] )
                    break
                case "USW8":
                    addChildDevice( "UnifiNetworkChild-USW8", DNI, [ name: "${ DNI }" ] )
                    break
                case "US8":
                    addChildDevice( "UnifiNetworkChild-US8", DNI, [ name: "${ DNI }" ] )
                    break
                case "UP1":
                case "Plug":
                    addChildDevice( "UnifiNetworkChild-Plug", DNI, [ name: "${ DNI }" ] )
                    break
                case "UGW3":
                    addChildDevice( "UnifiNetworkChild-UGW3", DNI, [ name: "${ DNI }" ] )
                    break
                case "UGW4":
                    addChildDevice( "UnifiNetworkChild-UGW4", DNI, [ name: "${ DNI }" ] )
                    break
                case "USG":
                    addChildDevice( "UnifiNetworkChild-USG", DNI, [ name: "${ DNI }" ] )
                    break
                case "UX":
                    addChildDevice( "UnifiNetworkChild-UX", DNI, [ name: "${ DNI }" ] )
                    break
                case "UXG":
                    addChildDevice( "UnifiNetworkChild-UXG", DNI, [ name: "${ DNI }" ] )
                    break
                case "UXGB":
                    addChildDevice( "UnifiNetworkChild-UXG", DNI, [ name: "${ DNI }" ] )
                    PauseExecution( 1000 )
                    getChildDevice( "${ DNI }" ).SetDefaults( true )
                    break
                case "UCGMax":
                    addChildDevice( "UnifiNetworkChild-UCGMax", DNI, [ name: "${ DNI }" ] )
                    break
                case "USAGGPRO":
                    addChildDevice( "UnifiNetworkChild-USAGGPRO", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM16":
                    addChildDevice( "UnifiNetworkChild-USPM16", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM16P":
                    addChildDevice( "UnifiNetworkChild-USPM16P", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM24":
                    addChildDevice( "UnifiNetworkChild-USPM24", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM24P":
                    addChildDevice( "UnifiNetworkChild-USPM24P", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM48":
                    addChildDevice( "UnifiNetworkChild-USPM48", DNI, [ name: "${ DNI }" ] )
                    break
                case "USPM48P":
                    addChildDevice( "UnifiNetworkChild-USPM48P", DNI, [ name: "${ DNI }" ] )
                    break
                default:
                    addChildDevice( "UnifiNetworkChild", DNI, [ name: "${ DNI }" ] )
                    break
            }
        } else {
            addChildDevice( "UnifiNetworkChild-Presence", DNI, [ name: "${ DNI }" ] )
        }
    }
    catch( Exception e ){
        def Temp = e as String
        if( Temp.contains( "not found" ) ){
            Logging( "UnifiNetworkChild-${ ChildType } driver is not loaded for ${ DNI }.", 5 )
        } else {
            Logging( "addChild error, likely ${ DNI } already exists: ${ Temp }", 5 )
        }
    }
}

// Handles whether logging is enabled and thus what to put there.
def Logging( LogMessage, LogLevel ){
	// Add all messages as info logging
    if( ( LogLevel == 2 ) && ( LogType != "None" ) ){
        log.info( "${ device.displayName } - ${ LogMessage }" )
    } else if( ( LogLevel == 3 ) && ( ( LogType == "Debug" ) || ( LogType == "Trace" ) ) ){
        log.debug( "${ device.displayName } - ${ LogMessage }" )
    } else if( ( LogLevel == 4 ) && ( LogType == "Trace" ) ){
        log.trace( "${ device.displayName } - ${ LogMessage }" )
    } else if( LogLevel == 5 ){
        log.error( "${ device.displayName } - ${ LogMessage }" )
    }
}

// Checks drdsnell.com for the latest version of the driver
// Original inspiration from @cobra's version checking
def CheckForUpdate(){
    ProcessEvent( "DriverName", DriverName(), null, true )
    ProcessEvent( "DriverVersion", DriverVersion(), null, true )
    ProcessEvent( "DriverStatus", null, null, true )
	httpGet( uri: "https://www.drdsnell.com/projects/hubitat/drivers/versions.json", contentType: "application/json" ){ resp ->
        switch( resp.status ){
            case 200:
                if( resp.data."${ DriverName() }" ){
                    CurrentVersion = DriverVersion().split( /\./ )
                    if( resp.data."${ DriverName() }".version == "REPLACED" ){
                       ProcessEvent( "DriverStatus", "Driver replaced, please use ${ resp.data."${ state.DriverName }".file }", null, true )
                    } else if( resp.data."${ DriverName() }".version == "REMOVED" ){
                       ProcessEvent( "DriverStatus", "Driver removed and no longer supported.", null, true )
                    } else {
                        SiteVersion = resp.data."${ DriverName() }".version.split( /\./ )
                        if( CurrentVersion == SiteVersion ){
                            Logging( "Driver version up to date", 3 )
				            ProcessEvent( "DriverStatus", "Up to date" )
                        } else if( ( CurrentVersion[ 0 ] as int ) > ( SiteVersion [ 0 ] as int ) ){
                            Logging( "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", null, true )
                        } else if( ( CurrentVersion[ 1 ] as int ) > ( SiteVersion [ 1 ] as int ) ){
                            Logging( "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", null, true )
                        } else if( ( CurrentVersion[ 2 ] as int ) > ( SiteVersion [ 2 ] as int ) ){
                            Logging( "Patch development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Patch development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", null, true )
                        } else if( ( SiteVersion[ 0 ] as int ) > ( CurrentVersion[ 0 ] as int ) ){
                            Logging( "New major release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New major release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", null, true )
                        } else if( ( SiteVersion[ 1 ] as int ) > ( CurrentVersion[ 1 ] as int ) ){
                            Logging( "New minor release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New minor release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", null, true )
                        } else if( ( SiteVersion[ 2 ] as int ) > ( CurrentVersion[ 2 ] as int ) ){
                            Logging( "New patch ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New patch ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", null, true )
                        }
                    }
                } else {
                    Logging( "${ DriverName() } is not published on drdsnell.com", 2 )
                    ProcessEvent( "DriverStatus", "${ DriverName() } is not published on drdsnell.com", null, true )
                }
                break
            default:
                Logging( "Unable to check drdsnell.com for ${ DriverName() } driver updates.", 2 )
                break
        }
    }
}
