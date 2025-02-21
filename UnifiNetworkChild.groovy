/*
* UnifiNetworkChild
*
* Description:
* This Hubitat driver provides a spot to put data from general Unifi devices or clients
*
* Instructions for using Tile Template method (originally based on @mircolino's HTML Templates):
* 1) In "Hubitat -> Devices" select the child/sensor (not the parent) you would like to "templetize"
* 2) In "Preferences -> Tile Template" enter your template (example below) and click "Save Preferences"
*   Ex: "[font size='2'][b]Temperature:[/b] ${ temperature }°${ location.getTemperatureScale() }[/br][/font]"
* 3) In a Hubitat dashboard, add a new tile, and select the child/sensor, in the center select "Attribute", and on the right select the "Tile" attribute
* 4) Select the Add Tile button and the tile should appear
* NOTE: Should accept most HTML formatting commands with [] instead of <>
* 
* Features List:
* Ability to check a website (mine) to notify user if there is a newer version of the driver available
* 
* Licensing:
* Copyright 2024 David Snell
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* Version Control:
* 0.2.9 - Removed "Driver Status" and added RestartDevice command
* 0.2.8 - Correction to ProcessEvent function and removal of old driver-specific attributes when Preferences are saved
* 0.2.7 - Added Uptime as an attribute
* 0.2.6 - Minor change to allow forcing an Event changed notice
* 0.2.5 - Update to driver update checking
* 0.2.4 - Correction to Tile Template preference
* 0.2.3 - Added refresh capability to get device specific information and revised HTML to Tile Template
* 0.2.2 - Change to newer method for driver update checking, also separating out driver types
* 0.2.1 - Correction to driver update checking code
* 0.2.0 - Changed naming and update mechanism to be for Network primarily
* 0.1.1 - Made it so check for new driver happens daily
* 0.1.0 - Initial version
* 
* Thank you(s):
* @Cobra for inspiration of how I perform driver version checking
* @mircolino for HTML Template method for dashboard use
*  for providing the basis for much of what is possible with the API
*/

// Returns the driver name
def DriverName(){
    return "UnifiNetworkChild"
}

// Returns the driver version
def DriverVersion(){
    return "0.2.9"
}

// Driver Metadata
metadata{
	definition( name: "UnifiNetworkChild", namespace: "Snell", author: "David Snell", importUrl: "https://www.drdsnell.com/projects/hubitat/drivers/UnifiNetworkChild.groovy" ) {
        capability "PresenceSensor" // Adds an attribute "presence" with possible values of "present" or "not present"
        capability "Actuator"
        capability "Refresh"
        
        // Commands
        command "RestartDevice"

        //command "DoSomething" // For testing and development purposes only, it should not be uncommented for normal use
        
        // Attributes - Driver Related
        attribute "DriverName", "string" // Identifies the driver being used for update purposes
        attribute "DriverVersion", "string" // Handles version for driver
        attribute "DriverStatus", "string" // Handles version notices for driver
        // Attributes - Device Related
        attribute "Last Seen", "string" // Date/Time the device was last seen by the Unifi controller
        attribute "Uptime", "string"
        
        // Tile Template attribute
        attribute "Tile", "string"; // Ex: "[font size='2'][b]Temperature:[/b] ${ temperature }°${ location.getTemperatureScale() }[/br][/font]"
        
    }
	preferences{
		//section{
            if( ShowAllPreferences ){
                input( name: "TileTemplate", type: "string", title: "<b>Tile Template</b>", description: "<font size='2'>Ex: [b]Temperature:[/b] \${ state.temperature }&deg;${ location.getTemperatureScale() }[/br]</font>", defaultValue: "");
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
    if( state."Driver Status" != null ){
        state.remove( "Driver Name" )
        state.remove( "Driver Version" )
        state.remove( "Driver Status" )
        device.deleteCurrentState( "Driver Status" )
        device.deleteCurrentState( "Driver Name" )
        device.deleteCurrentState( "Driver Version" )
    }
    ProcessState( "DriverName", "${ DriverName() }" )
    ProcessState( "DriverVersion", "${ DriverVersion() }" )
    
    def Hour = ( new Date().format( "h" ) as int )
    def Minute = ( new Date().format( "m" ) as int )
    def Second = ( new Date().format( "s" ) as int )
    // Schedule checks that are only performed once a day
    schedule( "${ Second } ${ Minute } ${ Hour } ? * *", "CheckForUpdate" )
    
    Logging( "Updated", 2 )
}

// DoSomething is for testing and development purposes. It should not be uncommented for normal usage.
def DoSomething(){

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

// Process data to check against current state value and then send an event if it has changed
def SetDeviceType( Type ){
    DeviceType = Type
    Logging( "DeviceType = ${ DeviceType }", 4 )
}

// Return a state value
def ReturnState( Variable ){
    return state."${ Variable }"
}

// Tile Template method based on @mircolino's HTML Template method
private void UpdateTile( String val ){
    if( settings.TileTemplate ){
        // Create special compound/html tile
        val = settings.TileTemplate.toString().replaceAll( "\\[", "<" )
        val = val.replaceAll( "\\]", ">" )
        val = val.replaceAll( ~/\$\{\s*([A-Za-z][A-Za-z0-9_]*)\s*\}/ ) { java.util.ArrayList m -> device.currentValue("${ m [ 1 ] }").toString() }
        if( device.currentValue( "Tile" ).toString() != val ){
            sendEvent( name: "Tile", value: val )
        }
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
