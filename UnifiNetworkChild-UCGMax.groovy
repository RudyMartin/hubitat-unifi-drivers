/*
* UnifiNetworkChild-UCGMax
*
* Description:
* This Hubitat driver provides a spot to put data from Unifi Cloud Gateway Max device
*
* Instructions for using Tile method:
* 1) In "Preferences -> Tile Template" enter your template (example below) and click "Save Preferences"
*   Ex: "[b]Temperature:[/b] @temperature@°@location.getTemperatureScale()@[/br]"
* 2) In a Hubitat dashboard, add a new tile, and select the child/sensor, in the center select "Attribute", and on the right select the "Tile" attribute
* 3) Select the Add Tile button and the tile should appear
* NOTE1: Put a @ before and after variable names
* NOTE2: Should accept most HTML formatting commands with [] instead of <>
* 
* Features List:
* Ability to check a website (mine) to notify user if there is a newer version of the driver available
* 
* Licensing:
* Copyright 2025 David Snell
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* Version Control:
* 0.1.0 - Initial version
* 
* Thank you(s):
* Thank you to @Cobra for inspiration of how I perform driver version checking
* Thank you to @mircolino for working out a parent/child method and pointing out other areas for significant improvement as well as coming up with the
*   original (no longer used) Tile/HTML Template method
*/

// Returns the driver name
def DriverName(){
    return "UnifiNetworkChild-UCGMax"
}

// Returns the driver version
def DriverVersion(){
    return "0.1.0"
}

// Driver Metadata
metadata{
	definition( name: "UnifiNetworkChild-UCGMax", namespace: "Snell", author: "David Snell", importUrl: "https://www.drdsnell.com/projects/hubitat/drivers/UnifiNetworkChild-UCGMax.groovy" ) {
        capability "PresenceSensor" // Adds an attribute "presence" with possible values of "present" or "not present"
        capability "TemperatureMeasurement"
        capability "Actuator"
        capability "Refresh"
        
        // Commands
        command "StartLocateDevice"
        command "StopLocateDevice"
        command "LEDOn"
        command "LEDOff"
        command "SetLEDColor",  [ [ name: "RGB*", type: "STRING", description: "RGB Color (HTML: 000000 to FFFFFF format)" ] ]
        command "SetLEDBrightness",  [ [ name: "Brightness*", type: "NUMBER", constraints: [ 0 .. 100 ], description: "Brightness from 0 to 100" ] ]
        command "RestartDevice"
        
        // Attributes - Driver Related
        attribute "DriverName", "string" // Identifies the driver being used for update purposes
        attribute "DriverVersion", "string" // Handles version for driver
        attribute "DriverStatus", "string" // Handles version notices for driver
        // Attributes - Device Related
        attribute "Last Seen", "string" // Date/Time the device was last seen by the Unifi controller
        attribute "Memory Usage", "number"
        attribute "CPU Usage", "number"
        attribute "Overheating", "string"
        attribute "Satisfaction", "number"
        attribute "Model LTS", "string"
        attribute "Model EOL", "string"
        attribute "CPU Temperature", "number"
        attribute "PHY Temperature", "number"
        attribute "Local Temperature", "number"
        attribute "LED Override", "string"
        attribute "LED Color", "string"
        attribute "Speedtest Download", "number"
        attribute "Speedtest Upload", "number"
        attribute "Uplink", "map"
        attribute "Port 01 Status", "map"
        attribute "Port 02 Status", "map"
        attribute "Port 03 Status", "map"
        attribute "Port 04 Status", "map"
        attribute "Port 05 Status", "map"
        attribute "Uptime", "string"
        attribute "Last_WAN_IP", "string"
        attribute "WAN1 Average Latency", "number"
        attribute "WAN1 Availability", "number"
        attribute "WAN2 Average Latency", "number"
        attribute "WAN2 Availability", "number"
        attribute "IP", "string"
        attribute "DeviceVersion", "string"

        // Tile Template attribute
        attribute "Tile", "string"; // Ex: "[b]Temperature:[/b] @temperature@°@location.getTemperatureScale()@[/br]"

    }
	preferences{
		//section{
            if( ShowAllPreferences ){
                if( state.DeviceName != null ){
                    input( type: "string", name: "DeviceName", title: "<b>Device Name</b>", description: "<font size='2'>If set it will change the device's name on the controller.</font>", defaultValue: "${ state.DeviceName }" )
                } else {
                    input( type: "string", name: "DeviceName", title: "<b>Device Name</b>", description: "<font size='2'>If set it will change the device's name on the controller.</font>", defaultValue: "" )
                }
                input( name: "TileTemplate", type: "string", title: "<b>Tile Template</b>", description: "<font size='2'>Ex: [b]Temperature:[/b] @temperature@&deg;@location.getTemperatureScale()@[/br]</font>", defaultValue: "");
    			input( type: "enum", name: "LogType", title: "<b>Enable Logging?</b>", required: false, multiple: false, options: [ "None", "Info", "Debug", "Trace" ], defaultValue: "Info" )
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            } else {
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            }
        //}
	}
}

// SetDefaults sets "unknown" as initial value for some attributes of the device
def SetDefaults( boolean isMax = false ){
    for( int x = 1; x <= 5; x++ ){
        ProcessState( "Port 0${ x } Status",  [ PortID: x, Enabled: "unknown", Media: "unknown", PoE: "unknown", PoE_Status: "unknown", PoE_Usage: "unknown", Speed: 0, Connected: "unknown" ] )
    }
    ProcessState( "Uplink",  [ PortID: "unknown", Enabled: "unknown", Media: "unknown", Speed: 0, RemotePort: "unknown", UplinkDevice: "unknown" ] )
    ProcessEvent( "Last Seen", "Unknown" )
    ProcessEvent( "Overheating", "Unknown" )
    ProcessEvent( "Model LTS", "Unknown" )
    ProcessEvent( "Model EOL", "Unknown" )
    ProcessEvent( "LED Override", "Unknown" )
    ProcessEvent( "LED OnOff", "Unknown" )
}

// updated
def updated( boolean NewDevice = false ){
    ProcessState( "DriverName", "${ DriverName() }" )
    ProcessState( "DriverVersion", "${ DriverVersion() }" )
	ProcessState( "DriverStatus", null )
    
    if( LogType == null ){
        LogType = "Info"
    }
    
    if( NewDevice != true ){
        if( ( DeviceName != state.DeviceName ) ){
            SendSettings()
        }
    }
    
    def Hour = ( new Date().format( "h" ) as int )
    def Minute = ( new Date().format( "m" ) as int )
    def Second = ( new Date().format( "s" ) as int )
    // Schedule checks that are only performed once a day
    schedule( "${ Second } ${ Minute } ${ Hour } ? * *", "CheckForUpdate" )
    
    Logging( "Updated", 2 )
}

// RestartDevice attempts to restart device
def RestartDevice(){
    if( state.MAC != null  ){
        parent.RestartDevice( state.MAC )
    } else {
        Logging( "No MAC known for ${ device.getDeviceNetworkId() }, cannot restart.", 5 )
    }
}

// Refresh the specific device's information
def refresh(){
    if( state.MAC != null  ){
        parent.RefreshSpecificUnifiDevice( state.MAC )
    } else {
        Logging( "No MAC for ${ device.getDeviceNetworkId() }, cannot refresh.", 5 )
    }
}

// Set the color of the LED
def SetLEDColor( String RGB ){
    if( state.ID != null ){
        parent.SetLEDColor( device.getDeviceNetworkId(), state.ID, state.MAC, RGB )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot set the device's LED.", 5 )
    }
}

// Set the brightness of the LED
def SetLEDBrightness( Number Brightness ){
    if( state.ID != null ){
        parent.SetLEDBrightness( device.getDeviceNetworkId(), state.ID, state.MAC, Brightness )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot set the device's LED.", 5 )
    }
}

// Starts the device location method
def StartLocateDevice(){
    if( state.MAC != null ){
        parent.LocateDevice( state.MAC, "On" )
    } else {
        Logging( "No MAC for ${ device.getDeviceNetworkId() }, cannot locate/identify.", 5 )
    }
}

// Stops the device location method
def StopLocateDevice(){
    if( state.MAC != null ){
        parent.LocateDevice( state.MAC, "Off" )
    } else {
        Logging( "No MAC for ${ device.getDeviceNetworkId() }, cannot locate/identify.", 5 )
    }
}


// Turn on the device's status LED(s)
def LEDOn(){
    if( state.ID != null ){
        parent.SetLEDOnOff( device.getDeviceNetworkId(), state.ID, state.MAC, "On" )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot set the device's LED.", 5 )
    }
}

// Turn off the device's status LED(s)
def LEDOff(){
    if( state.ID != null ){
        parent.SetLEDOnOff( device.getDeviceNetworkId(), state.ID, state.MAC, "Off" )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot set the device's LED.", 5 )
    }
}

// Configure device settings based on Preferences
def SendSettings(){
    if( state.ID != null ){
        def Settings = ""
        if( DeviceName != null && DeviceName != device.label ){
            parent.SendChildSettings( device.getDeviceNetworkId(), state.ID, "{\"name\":\"${ DeviceName }\"${ Settings } }" )
        } else {
            parent.SendChildSettings( device.getDeviceNetworkId(), state.ID, "{\"name\":\"${ device.label }\"${ Settings } }" )
        }
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot send settings", 5 )
    }
}

// installed is called when the device is installed, all it really does is run updated
def installed(){
	Logging( "Installed", 2 )
    SetDefaults( false )
	updated( true )
}

// initialize is called when the device is initialized, all it really does is run updated
def initialize(){
	Logging( "Initialized", 2 )
	updated( true )
}

// Return a state value
def ReturnState( Variable ){
    return state."${ Variable }"
}

// Tile method to produce HTML formatted string for dashboard use
private void UpdateTile( String val ){
    if( TileTemplate != null ){
        def TempString = ""
        Parsing = TileTemplate
        Parsing = Parsing.replaceAll( "\\[", "<" )
        Parsing = Parsing.replaceAll( "\\]", ">" )
        Count = Parsing.count( "@" )
        if( Count >= 1 ){
            def x = 1
            while( x <= Count ){
                TempName = Parsing.split( "@" )[ x ]
                switch( TempName ){
                    case "location.latitude":
                        Value = location.latitude
                        break
                    case "location.longitude":
                        Value = location.longitude
                        break
                    case "location.getTemperatureScale()":
                        Value = location.getTemperatureScale()
                        break
                    default:
                        Value = ReturnState( "${ TempName }" )
                        break
                }
                TempString = TempString + Parsing.split( "@" )[ ( x - 1 ) ] + Value
                x = ( x + 2 )
            }
            if( Parsing.split( "@" ).last() != Parsing.split( "@" )[ Count - 1 ] ){
                TempString = TempString + Parsing.split( "@" ).last()
            }
        } else if( Count == 1 ){
            TempName = Parsing.split( "@" )[ 1 ]
            switch( TempName ){
                case "location.latitude":
                    Value = location.latitude
                    break
                case "location.longitude":
                    Value = location.longitude
                    break
                case "location.getTemperatureScale()":
                    Value = location.getTemperatureScale()
                    break
                default:
                    Value = ReturnState( "${ TempName }" )
                    break
            }
            TempString = TempString + Parsing.split( "@" )[ 0 ] + Value
        } else {
            TempString = TileTemplate    
        }
        sendEvent( name: "Tile", value: TempString, isStateChange: true )
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
       UpdateTile( "${ Value }" )
    }
}

// Process data to check against current state value
def ProcessState( Variable, Value ){
    if( state."${ Variable }" != Value ){
        Logging( "State: ${ Variable } = ${ Value }", 4 )
        state."${ Variable }" = Value
        UpdateTile( "${ Value }" )
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
    ProcessEvent( "DriverName", DriverName() )
    ProcessEvent( "DriverVersion", DriverVersion() )
	httpGet( uri: "https://www.drdsnell.com/projects/hubitat/drivers/versions.json", contentType: "application/json" ){ resp ->
        switch( resp.status ){
            case 200:
                if( resp.data."${ DriverName() }" ){
                    CurrentVersion = DriverVersion().split( /\./ )
                    if( resp.data."${ DriverName() }".version == "REPLACED" ){
                       ProcessEvent( "DriverStatus", "Driver replaced, please use ${ resp.data."${ state.DriverName }".file }" )
                    } else if( resp.data."${ DriverName() }".version == "REMOVED" ){
                       ProcessEvent( "DriverStatus", "Driver removed and no longer supported." )
                    } else {
                        SiteVersion = resp.data."${ DriverName() }".version.split( /\./ )
                        if( CurrentVersion == SiteVersion ){
                            Logging( "Driver version up to date", 3 )
				            ProcessEvent( "DriverStatus", "Up to date" )
                        } else if( ( CurrentVersion[ 0 ] as int ) > ( SiteVersion [ 0 ] as int ) ){
                            Logging( "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version" )
                        } else if( ( CurrentVersion[ 1 ] as int ) > ( SiteVersion [ 1 ] as int ) ){
                            Logging( "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version" )
                        } else if( ( CurrentVersion[ 2 ] as int ) > ( SiteVersion [ 2 ] as int ) ){
                            Logging( "Patch development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 3 )
				            ProcessEvent( "DriverStatus", "Patch development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version" )
                        } else if( ( SiteVersion[ 0 ] as int ) > ( CurrentVersion[ 0 ] as int ) ){
                            Logging( "New major release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New major release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available" )
                        } else if( ( SiteVersion[ 1 ] as int ) > ( CurrentVersion[ 1 ] as int ) ){
                            Logging( "New minor release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New minor release ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available" )
                        } else if( ( SiteVersion[ 2 ] as int ) > ( CurrentVersion[ 2 ] as int ) ){
                            Logging( "New patch ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available", 2 )
				            ProcessEvent( "DriverStatus", "New patch ${ SiteVersion[ 0 ] }.${ SiteVersion[ 1 ] }.${ SiteVersion[ 2 ] } available" )
                        }
                    }
                } else {
                    Logging( "${ DriverName() } is not published on drdsnell.com", 2 )
                    ProcessEvent( "DriverStatus", "${ DriverName() } is not published on drdsnell.com" )
                }
                break
            default:
                Logging( "Unable to check drdsnell.com for ${ DriverName() } driver updates.", 2 )
                break
        }
    }
}
