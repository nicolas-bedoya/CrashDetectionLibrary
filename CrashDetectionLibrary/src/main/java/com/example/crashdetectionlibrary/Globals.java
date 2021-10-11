package com.example.crashdetectionlibrary;

public interface Globals {
    public static String ACCELERATION_DATA_FILE_NAME = "AccelerationData.txt";
    public static String GYROSCOPE_DATA_FILE_NAME = "GyroscopeData.txt";
    public static String USER_CONTACT_FILE_NAME = "UserContactInfo.txt"; // stores user contact info
    public static String EMERGENCY_CONTACT_FILE_NAME = "EmergencyContactInfo.txt"; // stores emergency contact info

    public static String ACTIVATE_SENSOR_REQUEST = "activate-sensor-request";
    public static String START_CRASH_DETECTION_CHECK = "start-crash-detection-check";
    public static String UNREGISTER_SENSOR_REQUEST = "unregister-sensor-request";
    public static String END_CRASH_CHECK = "end-crash-check";
    public static String DISMISS_ALERT_DIALOG = "dismiss-alert-dialog";

    public static int GYROSCOPE_ID = 1;
    public static int ACCELERATION_ID = 2;
    public static int IMPACT_TRUE = 1;

    public static String ALERT_DIALOG_REQUEST = "alert-dialog-request";


}
