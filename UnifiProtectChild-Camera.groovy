/*
* UnifiProtectChild-Camera
*
* Description:
* This Hubitat driver provides a spot to put data from Unifi Protect Camera-related devices. It does not belong on it's own and requires
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
* Ability to control general device settings
* Ability to trigger device's locate function
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
* 0.1.13 - Added RefreshRate and imageURL to assist with Easy Dashboard, as well as ImageAsOf and updated event/state handling
* 0.1.12 - Update to the Tile method and attributes for WebHook processing
* 0.1.11 - Added attributes for 3rd-party cameras
* 0.1.10 - Remove "Driver Status"
* 0.1.9 - Correction of isStateChange and driver-specific attributes
* 0.1.8 - Update to getting status for the device
* 0.1.7 - Added handling for forcing isChanged for events
* 0.1.6 - Added attribute for Last Motion
* 0.1.5 - Added attribute for smartDetectType
* 0.1.4 - Get a snapshot image from the camera using the take() command
* 0.1.3 - Removed capabilities not applicable to cameras
* 0.1.2 - Added additional attributes
* 0.1.1 - Changes to update check method, replacement of Tile Template method, removed data processing, removed invalid Preferences
* 0.1.0 - Initial version
* 
* Thank you(s):
* Thank you to @Cobra for inspiration of how I perform driver version checking
* Thank you to @mircolino for working out a parent/child method and pointing out other areas for significant improvement as well as coming up with the
*   original (no longer used) Tile/HTML Template method
*/

// Returns the driver name
def DriverName(){
    return "UnifiProtectChild-Camera"
}

// Returns the driver version
def DriverVersion(){
    return "0.1.13"
}

// Driver Metadata
metadata{
	definition( name: "UnifiProtectChild-Camera", namespace: "Snell", author: "David Snell", importUrl: "https://www.drdsnell.com/projects/hubitat/drivers/UnifiProtectChild-Camera.groovy" ) {
        capability "Sensor"
        capability "Actuator"
        capability "MotionSensor"
        capability "ImageCapture"
        capability "ImageUrl"
        capability "Refresh"
        
        // Commands
        //command "SetBrightness", [ [ name: "Brightness*", type: "ENUM", defaultValue: "3", constraints: [ "0", "1", "2", "3", "4", "5", "6" ], description: "REQUIRED: Brightness level of the floodlight from 0 (off) to 6 (full brightness)" ] ]
        //command "DoSomething", [ [ name: "Name*", type: "STRING" ], [ name: "Value", type: "STRING" ] ] // For testing and development purposes only, it should not be uncommented for normal use
        //command "RemoveState", [ [ name: "Name*", type: "STRING" ] ] // For testing and development purposes only, it should not be uncommented for normal use
        //command "ProcessEvent", [ [ name: "Name*", type: "STRING" ], [ name: "Value*", type: "STRING" ] ] // For testing and development purposes only, it should not be uncommented for normal use
        //command "ProcessState", [ [ name: "Name*", type: "STRING" ], [ name: "Value*", type: "STRING" ] ] // For testing and development purposes only, it should not be uncommented for normal use
        //command "Locate" // Meant to help identify/locate the particular device by flashing the light
        
        // Attributes for the driver itself
		attribute "DriverName", "string" // Identifies the driver being used for update purposes
		attribute "DriverVersion", "string" // Handles version for driver
        attribute "DriverStatus", "string" // Handles version notices for driver

        // General Device Attributes
        attribute "Type", "string" // The type of device, per Ubiquiti data
        
        // Attributes - Device Related
        attribute "Status", "string" // Show success/failure of commands performed
        attribute "DeviceStatus", "string" // Show the current state of the device as reported by the controller

        // Device Attributes
        attribute "Dark", "enum", [ "true", "false" ] // Whether the light level is dark
        attribute "Indicator Enabled", "enum", [ "true", "false" ] // Whether the camera's indicator is enabled
        attribute "RecordingNow", "enum", [ "true", "false" ] // Whether the camera is recording this moment
        attribute "MotionEventsToday", "number" // Number of motion events today
        attribute "SnapshotURL", "string"
        attribute "SnapshotImage", "string"
        attribute "smartDetectType", "string"
        attribute "LastMotion", "string"
        attribute "TestAttribute", "string"
        attribute "ImageAsOf", "string"
        
        // WebHook AI related Attributes
        attribute "AIRecognitionType", "string" // Stores the Type of AI detection triggered
        attribute "AIRecognitionValue", "string" // Stores the ID associated with a persons face as recognized by AI
        
        // Attributes to help with Easy Dashboards
        //attribute "imageUrl", "string" // 
        attribute "refreshRate", "string" // 
        
        // 3rd Party Attributes
        attribute "ThirdPartyCameraInfo", "string"
        attribute "ThirdPartyCamera", "string"
        
        // Tile Template attribute
        attribute "Tile", "string"; // Ex: "[b]Temperature:[/b] @temperature@°@location.getTemperatureScale()@[/br]"
        
    }
	preferences{
		//section{
            if( ShowAllPreferences ){
                input( type: "enum", name: "RefreshRate", title: "<b>Image Refresh Rate</b>", required: true, multiple: false, options: [ "Manual", "30 seconds", "1 minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "3 hours" ], defaultValue: "Manual" )
                input( type: "number", name: "MicVolume", title: "<b>Microphone Volume</b>", description: "<font size='2'>0 to 100</font>", required: true, defaultValue: 100, range: "0..100" )
                input( type: "bool", name: "StatusLED", title: "<b>Status LED On/Off</b>", defaultValue: false)
                input( type: "bool", name: "ExternalIR", title: "<b>External IR Lights On/Off</b>", defaultValue: false)
                input( type: "bool", name: "SystemSounds", title: "<b>System Status Sounds On/Off</b>", defaultValue: false)
                input( type: "string", name: "DeviceName", title: "<b>Device Name</b>", description: "<font size='2'>If set it will change the device's name on the controller.</font>", defaultValue: "${ device.label }")
                input( name: "TileTemplate", type: "string", title: "<b>Tile Template</b>", description: "<font size='2'>Ex: [b]Temperature:[/b] @temperature@&deg;@location.getTemperatureScale()@[/br]</font>", defaultValue: "");
    			input( type: "enum", name: "LogType", title: "<b>Enable Logging?</b>", required: false, multiple: false, options: [ "None", "Info", "Debug", "Trace" ], defaultValue: "Info" )
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            } else {
                input( type: "bool", name: "ShowAllPreferences", title: "<b>Show All Preferences?</b>", defaultValue: true )
            }
        //}
	}
}

// DoSomething is only for pre-publish code testing
def DoSomething( Name, Value = null ){

}

// RemoveState is only for pre-publish code testing
def RemoveState( Name ){
    if( Name != null ){
        state."${ Name }" = null
        state.remove( Name )
    }
}

// updated
def updated( boolean NewDevice = false ){
    if( LogType == null ){
        LogType = "Info"
    }
    
    if( NewDevice != true ){
        SendSettings()
    }
    
    ProcessEvent( "DriverName", DriverName(), null, true )
    ProcessEvent( "DriverVersion", DriverVersion(), null, true )
    ProcessEvent( "DriverStatus", null, null, true )
    
    // Schedule daily check for driver updates to notify user
    def Hour = ( new Date().format( "h" ) as int )
    def Minute = ( new Date().format( "m" ) as int )
    def Second = ( new Date().format( "s" ) as int )
        
    // Check what the refresh rate is set for then run it
    switch( RefreshRate ){
        case "30 seconds": // Schedule the camera to take a new image every 30 seconds
            schedule( "0/30 * * ? * *", "take" )
            break
        case "1 minute": // Schedule the camera to take a new image every minute
            schedule( "${ Second } * * ? * *", "take" )
            break
        case "5 minutes": // Schedule the camera to take a new image every 5 minutes
            schedule( "${ Second } 0/5 * ? * *", "take" )
            break
        case "10 minutes": // Schedule the camera to take a new image every 10 minutes
            schedule( "${ Second } 0/10 * ? * *", "take" )
            break
        case "15 minutes": // Schedule the camera to take a new image every 15 minutes
            schedule( "${ Second } 0/15 * ? * *", "take" )
            break
        case "30 minutes": // Schedule the camera to take a new image every 30 minutes
            schedule( "${ Second } 0/30 * ? * *", "take" )
            break
        case "1 hour": // Schedule the camera to take a new image every hour
            schedule( "${ Second } ${ Minute } * ? * *", "take" )
            break
        case "3 hours": // Schedule the camera to take a new image every 3 hours
            schedule( "${ Second } ${ Minute } 0/3 ? * *", "take" )
            break
        default:
            RefreshRate = "Manual"
            break
    }
    Logging( "Camera refresh rate: ${ RefreshRate }", 4 )
    ProcessEvent( "refreshRate", RefreshRate, null, true )
    
    // If the device id is known, set default values for image, imageUrl, and Thumbnail since they will never actually change
    if( state.id != null ){
        ProcessEvent( "image", "http://${ location.hub.localIP }/local/${ state.id }", null, true )
        ProcessEvent( "imageUrl", "http://${ location.hub.localIP }/local/${ state.id }", null, true )
        ProcessState( "Thumbnail", "<img width=\"10%\" height=\"10%\" src=\"http://${ location.hub.localIP }/local/Camera_${ state.id }_Image.jpg\">" )
    }
    
    // Schedule checks that are only performed once a day
    schedule( "${ Second } ${ Minute } ${ Hour } ? * *", "CheckForUpdate" )
    
    Logging( "Updated", 2 )
}

// Attempts to trigger a snapshot image from the camera
def take(){
    if( state.ID != null ){
        parent.GetSnapshot( state.ID, device.getDeviceNetworkId() )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot get snapshot", 5 )
    } 
}

// refresh information on the specific child
def refresh(){
    if( state.ID != null ){
        parent.GetCameraStatus( device.getDeviceNetworkId(), state.ID )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot refresh", 5 )
    }
}

// Turn on the device's locate function to help locate/identify it
def Locate(){
    if( state.ID != null ){
        parent.LocateCamera( device.getDeviceNetworkId(), state.ID )
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot activate identify function", 5 )
    }
}

// Configure device settings based on Preferences
// Sample {"micVolume":100,"name":"G3 Instant","ledSettings":{"isEnabled":true},"osdSettings":{"isNameEnabled":false,"isDateEnabled":false,"isLogoEnabled":false,"isDebugEnabled":false},"ispSettings":{"isExternalIrEnabled":false},"speakerSettings":{"areSystemSoundsEnabled":false}}
def SendSettings(){
    if( state.ID != null ){
        if( DeviceName != null && DeviceName != device.label ){
            parent.SendCameraSettings( device.getDeviceNetworkId(), state.ID, "{\"micVolume\":${ MicVolume },\"name\":\"${ DeviceName }\",\"ledSettings\":{\"isEnabled\":${ StatusLED }},\"ispSettings\":{\"isExternalIrEnabled\":${ ExternalIR }},\"speakerSettings\":{\"areSystemSoundsEnabled\":${ SystemSounds }}}" )
        } else {
            parent.SendCameraSettings( device.getDeviceNetworkId(), state.ID, "{\"micVolume\":${ MicVolume },\"name\":\"${ device.label }\",\"ledSettings\":{\"isEnabled\":${ StatusLED }},\"ispSettings\":{\"isExternalIrEnabled\":${ ExternalIR }},\"speakerSettings\":{\"areSystemSoundsEnabled\":${ SystemSounds }}}" )
        }
    } else {
        Logging( "No ID for ${ device.getDeviceNetworkId() }, cannot send settings", 5 )
    }
}

// installed is called when the device is installed, all it really does is run updated
def installed(){
	Logging( "Installed", 2 )
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
