package com.example.crashdetectionlibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CreateLoadContacts extends AppCompatActivity implements Globals{
    public CreateLoadContacts() {}
    private static final String TAG = "ActivityCreateLoadContacts";

    String[] EmergencyDetails = new String[6];
    String[] UserDetails = new String[4];

    @SuppressLint("LongLogTag")
    public void LoadTxtSensorFile(String file, Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine())!=null) {
                sb.append(text).append("\n");
            }
            // print line by line of text file into terminal
            Log.d(TAG, sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    public void CreateUserContactTxtFile(Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(Globals.USER_CONTACT_FILE_NAME, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Log.d(TAG, "UserTxtFile saved!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    public void AddDataUserContactTxtFile(String userFirstName, String userLastName,
                                          String userPhoneNumber, Context context) {

        FileOutputStream fos = null;
        String txtUserFirstName = userFirstName + "\n";
        String txtUserLastName = userLastName + "\n";
        String txtUserPhoneNumber = userPhoneNumber + "\n";

        try {
            fos = context.openFileOutput(Globals.USER_CONTACT_FILE_NAME, MODE_APPEND);
            fos.write(txtUserFirstName.getBytes());
            fos.write(txtUserLastName.getBytes());
            fos.write(txtUserPhoneNumber.getBytes());
            Log.d(TAG,"User data saved");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Log.d(TAG, "txt Added");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    public void AddDataToEmergencyTxt(String FirstName1, String LastName1, String PhoneNumber1,
                                      String FirstName2, String LastName2, String PhoneNumber2,
                                      Context context) {

        String firstName1 = FirstName1 + "\n";
        String lastName1 = LastName1 + "\n";
        String phoneNumber1 = PhoneNumber1 + "\n";

        String firstName2 = FirstName2 + "\n";
        String lastName2 = LastName2 + "\n";
        String phoneNumber2 = PhoneNumber2 + "\n";

        FileOutputStream fos = null;
        // Add data line by line into text file
        try {
            fos = context.openFileOutput(Globals.EMERGENCY_CONTACT_FILE_NAME, MODE_APPEND);
            fos.write(firstName1.getBytes());
            fos.write(lastName1.getBytes());
            fos.write(phoneNumber1.getBytes());
            fos.write(firstName2.getBytes());
            fos.write(lastName2.getBytes());
            fos.write(phoneNumber2.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Log.d(TAG, "txt added");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @SuppressLint("LongLogTag")
    public String[] LoadEmergencyContactTxtFile(Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(Globals.EMERGENCY_CONTACT_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine())!=null) {
                sb.append(text).append("\n");
            }

            String[] contactDetails = sb.toString().split("\n",6);
            EmergencyDetails[0] = contactDetails[0];
            EmergencyDetails[1] = contactDetails[1];
            EmergencyDetails[2] = contactDetails[2];

            EmergencyDetails[3] = contactDetails[3];
            EmergencyDetails[4] = contactDetails[4];
            EmergencyDetails[5] = contactDetails[5];

            Log.d(TAG, EmergencyDetails[0] + " " + EmergencyDetails[1] + " " + EmergencyDetails[2]);
            Log.d(TAG, EmergencyDetails[3] + " " + EmergencyDetails[4] + " " + EmergencyDetails[5]);

            Log.d(TAG, sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return EmergencyDetails;
    }

    @SuppressLint("LongLogTag")
    public String[] LoadUserContactTxtFile(Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(Globals.USER_CONTACT_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine())!=null) {
                sb.append(text).append("\n");
            }

            String[] userContactDetails = sb.toString().split("\n",3);
            UserDetails[0] = userContactDetails[0];
            UserDetails[1] = userContactDetails[1];
            UserDetails[2] = userContactDetails[2];

            Log.d(TAG, UserDetails[0] + " " + UserDetails[1] + " " + UserDetails[2]);
            Log.d(TAG, sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return UserDetails;
    }

    @SuppressLint("LongLogTag")
    public void CreateSensorFile(String file, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(file, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Log.d(TAG, file + " created!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void AddDataToSensorTxt(double sensor[], String file, Context context) {
        String text = sensor[0] + ", " + sensor[1] + ", " + sensor[2] + "\n";
        FileOutputStream fos = null;

        try {

            fos = context.openFileOutput(file, MODE_APPEND);
            fos.write(text.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    //Log.d(TAG, "txt Added");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
