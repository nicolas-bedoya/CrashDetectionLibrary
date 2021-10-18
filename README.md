# Library documentation
## Activities in App
| Activities           | Description                                                                                       |
|----------------------|---------------------------------------------------------------------------------------------------|
| ActivityNotification | Declaration of notification                                                                       |
| ActivityService      | Activates sensors and location listeners                                                          |
| MainActivity         | Starts Service and Ends Service from button press on screen. Also  behaves as a broadcast Manager |

Note, the activities within the app do not need to be directly implemented within the Forcite app by name, however what is featured within each Activity is critical in the functionality of the library. 

## Activities in Library

| Activities          | Description                                                                                                                                                                                                                                                                                        |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AlertDialog         | Provides alert dialog prompting user for whether a crash has occurred or not. A 120 second timer is implemented, <br>for if there is no response then a crash is assumed.                                                                                                                          |
| CreateLoadContacts  | NOT USED CURRENTLY - Reads data from a generated text file for user/emergency details (first name, last name, <br>phone number) for custom details. For the time being, details are hard coded.                                                                                                    |
| Globals (interface) | Provides definition for global constants that are used through out the program.                                                                                                                                                                                                                    |
| NotificationBase    | Definition of service channel, allows for notifications to occur in background and foreground.                                                                                                                                                                                                     |
| NotificationType    | Provides definition for the 2 different notification present within the current implementation - definition of <br>service notification (Alerting user that functionality is occurring in the background) and crash notification <br>(alerting user whether they have experienced a crash or not). |
| SendSMS             | Sends SMS to emergency contacts with latitude and longitude of user's location, address, and a pinned location<br>with a link on google maps.                                                                                                                                                      |
| SensorLocation      | Processes values from location listener and sensor listener that are added within the arguments of specific <br>methods within the class. Provides outputs for whether impact is detected from the sensor/location listener or not.                                                                |

### How to add library into application?
In the build.gradle(:app), add the following line within dependencies :
Implementation project(“:CrashDetectionLibrary”)
For it to work, the features of each activity from the application must be added to the application you wish it to implement it in for complete functionality of SensorLocation. Otherwise, SendSMS, AlertDialog can be used independently of SensorLocation – as long as the input variables to the methods of the classes mentioned are added correctly

### Assessment and logic of CrashDetection 
ActivityService is a service which processes multiple flags to determine whether a crash has occurred. Consider it as the centre of the library/application. The flags are the following: 
Flag (BOOLEAN)	Description
impactGyroscopeDetected	Set to TRUE when a movement of 25 rad/s or greater is experienced 
impactAccelDetected	Set to TRUE when a movement of 35m/s^2 or greater is experienced 
impactVelocityDetected	Set to TRUE when a linear acceleration of 10m/s^2 or greater is experienced

#### Logic of Flags 
The analysis of determining when a flag should be TRUE is conducted in a library Activity known as SensorLocation, which is called within ActivityService to obtain the updated values of the flags. If either impactAccelDetected or impactGyroscopeDetected is set to TRUE, then a timer known as impactSensorTimer is initiated. This timer has a period of 3 seconds for the program to check if a velocity change has occurred (impactVelocityDetected = TRUE). Once the 3 seconds have passed, and both flags (impactAccelDetected OR impactGyroscopeDetected) and impactVelocityDetected are not TRUE, then they are reset back to FALSE to continue the assessment of the flags.
 If either impactAccelDetected or impactGyroscopeDetected is TRUE, as well as impactVelocityDetected is TRUE, then it is assumed that a crash has occurred. 

#### Crash Detected from Flags
If a crash is assumed to occur, a broadcast is sent to MainActivity with the filter name “Globals.ALERT_DIALOG_REQUEST”, which is defined as “alert-dialog-request”. An extra String Array is attached to the intent of alert-dialog-request called ‘Location-Packet.’ This String array obtains the following in chronological order 
-	Address of user
-	Latitude 
-	Longitude 
MainActivity obtains a broadcast receiver known as ‘mMessageReceiverAlertDialog’. It is used to receive the alert-dialog-request called from ActivityService. The broadcast receiver then calls AlertDialogAppear method from the AlertDialog Activity, which takes the following arguments in chronological order:
-	Context of MainActivity 
-	String[] Emergency Contact 1 Details
-	String[] Emergency Contact 2 Details 
-	String[] User Details
-	String [] LocationPacket (received from the broadcast receiver)
The contents of the contacts mentioned above obtain the following arguments in chronological order:
-	First name 
-	Last name 
-	Phone number
The broadcast receivers defined in MainActivity are crucial as it allows for communication from library to application.
Location packet has the following details 
-	Address
-	Latitude 
-	Longitude 
The address is obtained from calling the method of SensorLocation called getCompleteAddressString. The following input variables are required:
-	Context context
-	Double latitude
-	Double longitude

#### Alert Dialog
AlertDialog requires the following arguments for its method AlertDialogAppear: 
-	Context context
-	String[] Emergency1
o	First name 
o	Last name 
o	Phone number

-	String[] Emergency2
o	First name 
o	Last name 
o	Phone number

-	String[] User
o	First name 
o	Last name 
o	Phone number

-	String[] LocationPacket
o	Address 
o	Latitude
o	Longitude 

Alert Dialog appears in the MainActivity context (this context can be replaced with the context of where the user is to see the dialog within the app). The user is prompted with a question accompanied by two buttons – Have you experienced a crash? Yes | Dismiss.  
‘Yes’ confirms that the user is responsive however has experienced a crash. A text message is then sent to both emergency contacts with the following details found in the text message:

-	Address 
-	(latitude, longitude)
-	Link to google maps for where the user is exactly located
If the user does not respond to the alert dialog on screen; the same response as described above will be produced. 
‘Dismiss’ allows for the service to recommence as the service is shutdown once the alert dialog appears. This means that the application will continue to assess the data and reconfigure flags etc.

#### Sending SMS to Emergency Contacts 
The sending of SMS is only performed once AlertDialog confirms there is no response from user after 120 seconds. The class obtains a method sendSMS which takes the following arguments in chronological order from first to last:
-	String[] Emergency1
-	String[] Emergency2
-	String[] User
-	String[] LocationPacket
-	Context context

#### Understanding of Broadcasts
Broadcasts were used throughout the application to update values after specific events. Receivers were implemented within the MainActivity which listened for events. The receivers are listed below.
- mMessageReceiverAlertDialog
- mMessageReceiverStopService
- mMessageReceiverStartService 

The intent filter of mMessageReceiverAlertDialog is ALERT_DIALOG_REQUEST, found within the Globals interface. The Broadcast is called immediately after confirmation of the crash from ActivityService. The receiver calls the method AlertDialogAppear from the AlertDialog class. 

The intent filter of mMessageReceiverStopService is END_CRASH_CHECK from the Globals interface. It is called from the AlertDialog once a crash is entirely confirmed. A variable 'impactConfirmed' is updated to TRUE, which can be used in further implementation which confirms the event. For simplicity of demonstration, method sendSMS from class SendSMS is called.

The intent filter of mMessageReceiverStartService is ACTIVATE_SENSOR_REQUEST from the Globals interface. The receiver starts the service ActivityService from AlertDialog. This occurs when the user provides a response showing that there is no crash experienced, allowing for the service to recommence.

### Method of logic

-	MainActivity is launched as the first activity from the application
-	Two buttons are prompted on the screen: ‘Start Ride’ and ‘End Ride’
- ‘Start Ride’ pressed – starts ActivityService
      - ActivityService initialises listeners for location and sensor (gyroscope and acceleration) 
      - ActivityService continuously calls methods from SensorLocation of Library to update impact flags 
      - If either Gyroscope or acceleration flag is triggered, as well as velocity, then alertDialog from library is called through a broadcast sent to MainActivity of the               application
            - Alert dialog prompts user for a response to confirm that a crash has not occurred.
            - If no response was performed by the user, then a sms is sent to emergency contact with location details.
            - If all flags are not triggered after 3 seconds of the first flag being set to true, then the check continues as per normal 
- ‘End Ride’ pressed – ends Activity Service (used to replicate for when a user reaches their destination.
