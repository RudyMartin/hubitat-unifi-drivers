/*
* UnifiProtectChild
*
* Description:
* This Hubitat driver provides a spot to put data from Unifi Protect devices. It does not belong on it's own and requires
* the UnifiProtectAPI driver as a parent device.
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
* Support for NVR attributes
* Support for Bridge attributes
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
* 0.1.13 - Update to Event and State methods
* 0.1.12 - Update to the Tile method and attributes for WebHook processing
* 0.1.11 - Adding support for 3rd-party camera attributes
* 0.1.10 - Cleanup of "Device Status" attribute
* 0.1.9 - Correction of isStateChange and driver-specific attributes
* 0.1.8 - Handling for ProcessEvent with Forced isChanged
* 0.1.7 - Added attribute for Last Motion
* 0.1.6 - Additional attributes in case an unidentified device type uses the child driver until a proper one can be made
* 0.1.5 - Changes to update checking method, replacement of Tile Template method, and removed data processing
* 0.1.4 - Adjustments to driver update checking
* 0.1.3 - Removal of Light-specific attributes, replaced by specific child driver
* 0.1.2 - Correction to driver update checking code
* 0.1.1 - Additional capabilities and attributes for lights
* 0.1.0 - Initial version
* 
* Thank you(s):
* Thank you to @Cobra for inspiration of how I perform driver version checking
* Thank you to @mircolino for working out a parent/child method and pointing out other areas for significant improvement as well as coming up with the
*   original (no longer used) Tile/HTML Template method
*/

// Returns the driver name
def DriverName(){
    return "UnifiProtectChild"
}

// Returns the driver version
def DriverVersion(){
    return "0.1.13"
}

// Driver Metadata
metadata{
	definition( name: "UnifiProtectChild", namespace: "Snell", author: "David Snell", importUrl: "https://www.drdsnell.com/projects/hubitat/drivers/UnifiProtectChild.groovy" ) {
        capability "PresenceSensor" // Adds an attribute "presence" with possible values of "present" or "not present"
        capability "Sensor"
        //capability "Actuator"
        
        // Commands
        //command "DoSomething" // For testing and development purposes only, it should not be uncommented for normal use
        
        // Attributes for the driver itself
		attribute "DriverName", "string" // Identifies the driver being used for update purposes
		attribute "DriverVersion", "string" // Handles version for driver
        attribute "DriverStatus", "string" // Handles version notices for driver
        
        // Attributes - Device Related
        attribute "Status", "string" // Show success/failure of commands performed
        attribute "DeviceStatus", "string" // Show the current state of the device as reported by the controller
        
        // General Device Attributes
        attribute "Type", "string" // The type of device, per Ubiquiti data
        
        // NVR Specific Attributes
        attribute "Protect Version", "string" // Version of the Protect software running on the NVR
        attribute "CPU_Temp", "number" // Temperature of the NVR's cpu
        attribute "CPU_AverageLoad", "number" // % Average load of the NVR's cpu
        attribute "MemoryAvailable", "number" // % of the memory available
        attribute "StorageAvailable", "number" // % of the storage available
        
        // Attributes used by Cameras & Doorbells, in case an unidentified device type starts using them
        attribute "Dark", "enum", [ "true", "false" ] // Whether the light level is dark
        attribute "IndicatorEnabled", "enum", [ "true", "false" ] // Whether the camera's indicator is enabled
        attribute "RecordingNow", "enum", [ "true", "false" ] // Whether the camera is recording this moment
        attribute "MotionEventsToday", "number" // Number of motion events today
        attribute "ButtonPresses", "number" // ButtonPresses is used to show the number of button presses
        attribute "SnapshotURL", "string"
        attribute "SnapshotImage", "string"
        attribute "smartDetectType", "string"
        attribute "LastMotion", "string"
        
        // WebHook AI related Attributes
        attribute "AIRecognitionType", "string" // Stores the Type of AI detection triggered
        attribute "AIRecognitionValue", "string" // Stores the ID associated with a persons face as recognized by AI 
        
        // 3rd Party Attributes
        attribute "ThirdPartyCameraInfo", "string"
        attribute "ThirdPartyCamera", "string"
        
        // Tile Template attribute
        attribute "Tile", "string"; // Ex: "[b]Temperature:[/b] @temperature@°@location.getTemperatureScale()@[/br]"
        
    }
	preferences{
		//section{
            if( ShowAllPreferences ){
                input( name: "TileTemplate", type: "string", title: "<b>Tile Template</b>", description: "<font size='2'>Ex: [b]Temperature:[/b] @temperature@&deg;@location.getTemperatureScale()@[/br]</font>", defaultValue: "");
    			input( type: "enum", name: "LogType", title: "<b>Enable Logging?</b>", required: false, multiple: false, options: [ "None", "Info", "Debug", "Trace" ], defaultValue: "Info" )
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            } else {
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            }
        //}
	}
}

// updated
def updated(){   
    if( LogType == null ){
        LogType = "Info"
    }
    
    // Schedule daily check for driver updates to notify user
    def Hour = ( new Date().format( "h" ) as int )
    def Minute = ( new Date().format( "m" ) as int )
    def Second = ( new Date().format( "s" ) as int )
    
    // Set the driver name and version before update checking is scheduled
    if( state."Driver Status" != null ){
        state.remove( "Driver Name" )
        state.remove( "Driver Version" )
        state.remove( "Driver Status" )
        device.deleteCurrentState( "Driver Status" )
        device.deleteCurrentState( "Driver Name" )
        device.deleteCurrentState( "Driver Version" )
    }
    ProcessEvent( "DriverName", DriverName() )
    ProcessEvent( "DriverVersion", DriverVersion() )
    ProcessEvent( "DriverStatus", null )
    // Schedule checks that are only performed once a day
    schedule( "${ Second } ${ Minute } ${ Hour } ? * *", "CheckForUpdate" )    
    Logging( "Updated", 2 )
}

// DoSomething is for testing and development purposes. It should not be uncommented for normal usage.
def DoSomething(){

}

// installed is called when the device is installed, all it really does is run updated
def installed(){
	Logging( "Installed", 2 )
	updated()
}

// initialize is called when the device is initialized, all it really does is run updated
def initialize(){
	Logging( "Initialized", 2 )
	updated()
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
        Logging( "Tile = ${ TempString }", 4 )
        sendEvent( name: "Tile", value: TempString )
    }
}

// Process data to check against current state value and then send an event if it has changed
def ProcessEvent( Variable, Value, Unit = null, ForceEvent = false, Description = null ){
    if( ForceEvent ){
        sendEvent( name: Variable, value: Value, unit: Unit, isStateChange: true, descriptionText: Description )
    } else {
        sendEvent( name: Variable, value: Value, unit: Unit, descriptionText: Description )
    }
    Logging( "Event: ${ Variable } = ${ Value } Unit = ${ Unit } Forced = ${ ForceEvent }", 4 )
    ProcessState( Variable, Value )
    UpdateTile( "${ Value }" )
}

// Set a state variable to a value
def ProcessState( Variable, Value ){
    Logging( "State: ${ Variable } = ${ Value }", 4 )
    state."${ Variable }" = Value
    UpdateTile( "${ Value }" )
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
                            Logging( "Driver version up to date", 2 )
				            ProcessEvent( "DriverStatus", "Up to date" )
                        } else if( ( CurrentVersion[ 0 ] as int ) > ( SiteVersion [ 0 ] as int ) ){
                            Logging( "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 4 )
				            ProcessEvent( "DriverStatus", "Major development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version" )
                        } else if( ( CurrentVersion[ 1 ] as int ) > ( SiteVersion [ 1 ] as int ) ){
                            Logging( "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 4 )
				            ProcessEvent( "DriverStatus", "Minor development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version" )
                        } else if( ( CurrentVersion[ 2 ] as int ) > ( SiteVersion [ 2 ] as int ) ){
                            Logging( "Patch development ${ CurrentVersion[ 0 ] }.${ CurrentVersion[ 1 ] }.${ CurrentVersion[ 2 ] } version", 4 )
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
