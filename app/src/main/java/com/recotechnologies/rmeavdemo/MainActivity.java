// This header may not be removed.  It should accompany all source code
// derived from this code in any form or fashion.
// Filename: MainActivity.java
// Author: Homayoon Beigi <beigi@recotechnologies.com>
// Copyright (c) 2003-2021 Recognition Technologies, Inc.
// Date: June 9, 2020
// This code may be used as is and may not be distributed or copied without the
// explicit knowledge and permission of the author or Recognition Technologies, Inc.

package com.recotechnologies.rmeavdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.recotechnologies.rmeavapi.RMEAVAction;
import com.recotechnologies.rmeavapi.RMEAVClient;
import com.recotechnologies.rmeavapi.RMEAVEventListener;
import com.recotechnologies.rmeavapi.RMEAVListener;
import com.recotechnologies.rmeavapi.RMEAVLocal;
import com.recotechnologies.rmeavapi.RMEAVSettings;
import com.recotechnologies.rmeavapi.RMEAVSettingsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.recotechnologies.rmeavdemo.R.*;

public class MainActivity extends AppCompatActivity implements RMEAVListener, DialogEnrollClaimedID.DialogEnrollClaimedIDListener, DialogUnenrollClaimedID.DialogUnenrollClaimedIDListener, RMEAVEventListener
{
  //protected int m_iISCDebug = 0; // Normal Operation
  protected int m_iISCDebug = 1;
  //protected int m_iISCDebug = 2;
  //protected int m_iISCDebug = 3;

  // Set this to 1 to save a copy on the external storage
  private int m_iSaveLocalImage = 0;

  // Set this to true to stop the speech processing after each full result
  // is returned from the engine.
  private Boolean m_bStopSpeechAutomatically=false;
  private RMEUIState m_enumUIState=RMEUIState.NOT_LISTENING;

  //LOGGING
  private final String m_strClassTag = "RMEAVDemo";

  File sdcard = Environment.getExternalStorageDirectory();
  String sRMERootDir = sdcard.getAbsolutePath() + "/";

  // UI
  private Spinner m_spinnerAction;
  private Button m_btnAction;
  private Button m_btnStop;

  private List<RMEAVAction> m_listRMEAVAction;
  private TextView m_TextViewConsole, m_TextViewState;
  private ScrollView m_ScrollView;
  //  private Boolean m_bDialogHaveResults=false;
  private RMEAVAction m_sRMEAVAction;

  // Recognizer
  public RMEAVClient m_sRMEAVClient = null;
  public RMEAVLocal m_sRMEAVLocal = null;
  public RMEAudioAndroid m_sRMEAudioAndroid = null;
  public RMEImageAndroid m_sRMEImageAndroid = null;
  long m_hRMEAVEngineHandle = 0;
  long m_hRMEAVResourceHandle=0;

  public String m_strClaimedID = "";
  public String m_strClassDescription = "";
  private volatile String m_strTranscript = null;

  private RMECameraAndroid m_sRMECameraAndroid = null;
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(layout.activity_main);

    Toolbar toolbar = findViewById(id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setTitle(getString(string.app_name));
    getSupportActionBar().setIcon(getDrawable(mipmap.speechsignal_short));

    m_sRMEAVLocal = null;
    m_sRMEAudioAndroid = null;
    m_sRMEImageAndroid = null;
    m_sRMEAVClient = null;

    /* Request permissions to resources */
    ActivityCompat.requestPermissions(this,
                                      new String[]
                                      {
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                        WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                      }, 0);
 

     //Everything should be included in this one JNI:
    //    if (RMEAVSettings.RMEAV_LOCAL == true)
    if (GetSettingBoolean("pref_SwitchEmbeddedEngine", true) == true)
    {
      String strRootDir = GetSettingString("pref_EditTextEmbeddedRootDir", "/sdcard/rmeroot");
      String strRMEAVUserID = GetSettingString("pref_EditTextEmbeddedUserID", "rmeeng16k16");
      String strSpeechLanguage = GetSettingString("pref_ListRMESpchEmbeddedLanguage", "eng_us");
      String strSpeechModelVersion = GetSettingString("pref_ListRMESpchEmbeddedModelVersion", "Small");
      String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedDetectModelVersion", "Face");
      String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedRecoModelVersion", "Face");
      int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
      float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);
      float fSpeakerWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpkrWeight", 100) / 100.0);
      float fFaceWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMEFaceWeight", 100) / 100.0);
      Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
      Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
      Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);
      
      if (m_iISCDebug > 1)
      {
        Log.d(m_strClassTag, "Instantiating RMEAVLocal with the following settings:");
        Log.d(m_strClassTag, "strRootDir: "+strRootDir);
        Log.d(m_strClassTag, "strRMEAVUserID: "+strRMEAVUserID);
        Log.d(m_strClassTag, "strSpeechLanguage: "+strSpeechLanguage);
        Log.d(m_strClassTag, "strSpeechModelVersion: "+strSpeechModelVersion);
        Log.d(m_strClassTag, "strFaceDetectModelVersion: "+strFaceDetectModelVersion);
        Log.d(m_strClassTag, "strFaceRecoModelVersion: "+strFaceRecoModelVersion);
        Log.d(m_strClassTag, "iSamplingRate: "+iSamplingRate);
        Log.d(m_strClassTag, "fSpeechWeight: "+fSpeechWeight);
        Log.d(m_strClassTag, "fSpeakerWeight: "+fSpeakerWeight);
        Log.d(m_strClassTag, "fFaceWeight: "+fFaceWeight);
        Log.d(m_strClassTag, "bDoEndPointing: "+bDoEndpointing);
        Log.d(m_strClassTag, "bDoWordAlignment: "+bDoWordAlignment);
        Log.d(m_strClassTag, "bDoPhoneAlignment: "+bDoPhoneAlignment);
      }

      m_sRMEAVLocal =
        new RMEAVLocal(MainActivity.this,
                       getApplicationContext(),
                new RMEAVSettingsManager.Builder()
                        .setSamplingRate(iSamplingRate)
                        .setRootDir(strRootDir)
                        .setRMEAVUserID(strRMEAVUserID)
                        .setSpeechLanguage(strSpeechLanguage)
                        .setSpeechModelVersion(strSpeechModelVersion)
                        .setFaceDetectModelVersion(strFaceDetectModelVersion)
                        .setFaceRecoModelVersion(strFaceRecoModelVersion)
                        .setSpeechWeight(fSpeechWeight)
                        .setSpeakerWeight(fSpeakerWeight)
                        .setFaceWeight(fFaceWeight)
                        .setDoEndPointing(bDoEndpointing)
                        .setDoWordAlignment(bDoWordAlignment)
                        .setDoPhoneAlignment(bDoPhoneAlignment)
                        .setDebugLevel(m_iISCDebug)
                        .build());
      if (m_sRMEAVLocal.Error() != 0)
      {
        Log.d(m_strClassTag, m_sRMEAVLocal.ErrorString());
        m_sRMEAVLocal = null;
      }

      // LoadResource -- Load Models
      m_hRMEAVResourceHandle = m_sRMEAVLocal.LoadResource(new RMEAVSettingsManager.Builder()
                                                                .setSamplingRate(iSamplingRate)
                                                                .setRootDir(strRootDir)
                                                                .setRMEAVUserID(strRMEAVUserID)
                                                                .setSpeechLanguage(strSpeechLanguage)
                                                                .setSpeechModelVersion(strSpeechModelVersion)
                                                                .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                                                .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                                                .setSpeechWeight(fSpeechWeight)
                                                                .setSpeakerWeight(fSpeakerWeight)
                                                                .setFaceWeight(fFaceWeight)
                                                                .setDoEndPointing(bDoEndpointing)
                                                                .setDoWordAlignment(bDoWordAlignment)
                                                                .setDoPhoneAlignment(bDoPhoneAlignment)
                                                                .setDebugLevel(m_iISCDebug)
                                                                .build());

      if (fSpeechWeight > 0.0 || fSpeakerWeight > 0.0)
        m_sRMEAudioAndroid = new RMEAudioAndroid(m_sRMEAVLocal, m_iISCDebug);

//      if (fFaceWeight > 0.0)
//      {
//        m_sRMEImageAndroid = new RMEImageAndroid(m_sRMEAVLocal, m_iISCDebug);
//
//        int iWidthRequested = m_sRMEImageAndroid.GetWidthRequested();
//        int iHeightRequested = m_sRMEImageAndroid.GetHeightRequested();
//        int iNumChannelRequested = m_sRMEImageAndroid.GetNumChannelRequested();
//        int iNumBytePerChannelRequested = m_sRMEImageAndroid.GetNumBytePerChannelRequested();
//
//        // Camera BEGIN
//        if (null == savedInstanceState)
//        {
//          String strCameraDirection = GetSettingString("pref_ListCameraLensDirection", "front");
//          if (m_iISCDebug > 1)
//            Log.d(m_strClassTag, "pref_ListCameraLensDirection is " + strCameraDirection);
//
//          m_sRMECameraAndroid =
//            new RMECameraAndroid(m_sRMEImageAndroid,
//                                 strCameraDirection,
//                                 iWidthRequested,
//                                 iHeightRequested,
//                                 iNumChannelRequested,
//                                 iNumBytePerChannelRequested,
//                                 m_iISCDebug);
//
//          getSupportFragmentManager().beginTransaction()
//                                     .replace(id.cameracontainer,
//                                     m_sRMECameraAndroid).commit();
//        }
//        // Camera END
//      }
    }

    if (m_sRMEAVLocal == null)
    {
          String strServerAddress = GetSettingString("pref_EditTextServerAddress", "rmeav1.audiovisualreco.com");
          int iPort = Integer.parseInt(GetSettingString("pref_EditTextServerPort", "17201"));
          String strRMEAVUserID = GetSettingString("pref_EditTextClientServerUserID", "rmeeng16k16");
          String strSpeechLanguage = GetSettingString("pref_ListRMESpchClientServerLanguage", "eng_us");
          String strSpeechModelVersion = GetSettingString("pref_ListRMESpchClientServerModelVersion", "Huge");
          String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceClientServerDetectModelVersion", "Face");
          String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceClientServerRecoModelVersion", "Face");
          int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
          float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);
          float fSpeakerWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpkrWeight", 100) / 100.0);
          float fFaceWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMEFaceWeight", 100) / 100.0);
          Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
          Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
          Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

          m_sRMEAVClient = new RMEAVClient(MainActivity.this,
                                           getApplicationContext(),
                                           new RMEAVSettingsManager.Builder()
                                               .setSamplingRate(iSamplingRate)
                                               .setServerAddress(strServerAddress)
                                               .setServerPort(iPort)
                                               .setRMEAVUserID(strRMEAVUserID)
                                               .setSpeechLanguage(strSpeechLanguage)
                                               .setSpeechModelVersion(strSpeechModelVersion)
                                               .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                               .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                               .setSpeechWeight(fSpeechWeight)
                                               .setSpeakerWeight(fSpeakerWeight)
                                               .setFaceWeight(fFaceWeight)
                                               .setDoEndPointing(bDoEndpointing)
                                               .setDoWordAlignment(bDoWordAlignment)
                                               .setDoPhoneAlignment(bDoPhoneAlignment)
                                               .setDebugLevel(m_iISCDebug)
                                               .build());
    }
    

    /* Set up Resources -- Begin */

    m_spinnerAction = (Spinner) findViewById(id.spinnerAction);
    SetupSpinnerAction();
    
    m_btnAction = (Button) findViewById(id.btnAction);
    m_btnStop = (Button) findViewById(id.btnStop);
    m_ScrollView = (ScrollView) findViewById(id.scrollView);
    m_TextViewConsole = (TextView) findViewById(id.textViewConsole);
    m_TextViewState = (TextView) findViewById(id.textViewState);
    m_TextViewState.setText("Press LISTEN and Speak");

// RMEImage Begin
    /*
    m_sTextureView = (TextureView) findViewById(R.id.textureViewIdentify);
    m_sTextureView.setSurfaceTextureListener(m_sTextureViewListener);
    */
// RMEImage End

    m_btnAction.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        RMEAVAction sRMEAVActionRef =
          (RMEAVAction) m_spinnerAction.getSelectedItem();

        if (m_iISCDebug > 1)
          Log.d(m_strClassTag, "Recording Pressed!");
        if (m_enumUIState == RMEUIState.LISTENING)
          return;

        setUIState(RMEUIState.LISTENING);

        m_sRMEAVAction = null;
        m_sRMEAVAction = new RMEAVAction(sRMEAVActionRef);
        String strAction = m_sRMEAVAction.getStrAction();

        if (strAction.equals("Enroll"))
        {
          //            m_bDialogEnrollHaveResults = false;
          DialogEnrollGetClaimedID();
        }
        else if (strAction.equals("Unenroll"))
        {
          //            m_bDialogUnenrollHaveResults = false;
          DialogUnenrollGetClaimedID();
        }
        else
        {
          ClearScreen();
          if (m_sRMEAVLocal == null)
          {
            String strServerAddress = GetSettingString("pref_EditTextServerAddress", "rmeav1.audiovisualreco.com");
            int iPort = Integer.parseInt(GetSettingString("pref_EditTextServerPort", "17201"));
            String strRMEAVUserID = GetSettingString("pref_EditTextClientServerUserID", "rmeeng16k16");
            String strSpeechLanguage = GetSettingString("pref_ListRMESpchClientServerLanguage", "eng_us");
            String strSpeechModelVersion = GetSettingString("pref_ListRMESpchClientServerModelVersion", "Huge");
            String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceClientServerDetectModelVersion", "Face");
            String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceClientServerRecoModelVersion", "Face");
            int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
            float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);
            float fSpeakerWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpkrWeight", 100) / 100.0);
            float fFaceWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMEFaceWeight", 100) / 100.0);
            Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
            Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
            Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

            if (strAction.equals("Transcribe"))
            {
              fSpeechWeight = (float) 1.0;
              fSpeakerWeight = (float) 0.0;
              fFaceWeight = (float) 0.0;
            }
            else if (strAction.equals("Diarize"))
            {
              fSpeechWeight = (float) 1.0;
              fSpeakerWeight = (float) 1.0;
              fFaceWeight = (float) 0.0;
            }
            else if (strAction.equals("Segment"))
            {
              fSpeechWeight = (float) 0.0;
              fSpeakerWeight = (float) 1.0;
              fFaceWeight = (float) 0.0;
            }

            m_hRMEAVEngineHandle = m_sRMEAVClient.NewEngineInstance(m_sRMEAVAction,
                                      new RMEAVSettingsManager.Builder()
                                            .setSamplingRate(iSamplingRate)
                                            .setServerAddress(strServerAddress)
                                            .setServerPort(iPort)
                                            .setRMEAVUserID(strRMEAVUserID)
                                            .setSpeechLanguage(strSpeechLanguage)
                                            .setSpeechModelVersion(strSpeechModelVersion)
                                            .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                            .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                            .setSpeechWeight(fSpeechWeight)
                                            .setSpeakerWeight(fSpeakerWeight)
                                            .setFaceWeight(fFaceWeight)
                                            .setDoEndPointing(bDoEndpointing)
                                            .setDoWordAlignment(bDoWordAlignment)
                                            .setDoPhoneAlignment(bDoPhoneAlignment)
                                            .setDebugLevel(m_iISCDebug)
                                            .build());
          }
          else
          {
            String strRootDir = GetSettingString("pref_EditTextEmbeddedRootDir", "/sdcard/rmeroot");
            String strRMEAVUserID = GetSettingString("pref_EditTextEmbeddedUserID", "rmeeng16k16");
            String strSpeechLanguage = GetSettingString("pref_ListRMESpchEmbeddedLanguage", "eng_us");
            String strSpeechModelVersion = GetSettingString("pref_ListRMESpchEmbeddedModelVersion", "Small");
            String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedDetectModelVersion", "Face");
            String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedRecoModelVersion", "Face");
            int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
            float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);
            float fSpeakerWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpkrWeight", 100) / 100.0);
            float fFaceWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMEFaceWeight", 100) / 100.0);
            Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
            Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
            Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

            m_hRMEAVEngineHandle = m_sRMEAVLocal.NewEngineInstance(m_sRMEAVAction,
                                                                   m_hRMEAVResourceHandle,
                                                                   m_sRMEAudioAndroid,
                                                                   m_sRMEImageAndroid,
                                      new RMEAVSettingsManager.Builder()
                                            .setSamplingRate(iSamplingRate)
                                            .setRootDir(strRootDir)
                                            .setRMEAVUserID(strRMEAVUserID)
                                            .setSpeechLanguage(strSpeechLanguage)
                                            .setSpeechModelVersion(strSpeechModelVersion)
                                            .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                            .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                            .setSpeechWeight(fSpeechWeight)
                                            .setSpeakerWeight(fSpeakerWeight)
                                            .setFaceWeight(fFaceWeight)
                                            .setDoEndPointing(bDoEndpointing)
                                            .setDoWordAlignment(bDoWordAlignment)
                                            .setDoPhoneAlignment(bDoPhoneAlignment)
                                            .setDebugLevel(m_iISCDebug)
                                            .build());


            m_sRMEAudioAndroid.StartAudio(iSamplingRate,m_hRMEAVEngineHandle);

            if (m_sRMEAVLocal.EngineStart(m_hRMEAVEngineHandle) == false)
              Log.d(m_strClassTag, "Error Starting the Engine!");

          }


        }
      }
    });

    m_btnStop.setOnClickListener(v -> {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "Stop Pressed!");

      if (m_enumUIState == RMEUIState.NOT_LISTENING)
        return;

      setUIState(RMEUIState.NOT_LISTENING);
      StopSpeechActivities();

      m_sRMEAudioAndroid.StopAudio(m_hRMEAVEngineHandle);
    });

    /* Set up Resources -- End */

  } // End onCreate

  private void setEnabledLook(View view, boolean enabled)
  {
    view.setEnabled(enabled);
    Button btnButton = (Button) view;
    String strButtonText = btnButton.getText().toString();

    if (enabled)
    {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "setEnabledLook: Turning on Button "+strButtonText);
      view.setAlpha(1.0f);
    }
    else
    {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "setEnabledLook: Turning off Button "+strButtonText);
      view.setAlpha(0.5f);
    }
  }

  public synchronized void setUIState(final RMEUIState eUIState)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        m_enumUIState=eUIState;
        switch (eUIState)
        {
          case NOT_LISTENING:
            setEnabledLook(m_btnAction, true);
            setEnabledLook(m_btnStop, false);
            m_TextViewState.setText("Press Start and Speak");
            Toast.makeText(getApplicationContext(),
                           "Recording Stopped",
                           Toast.LENGTH_LONG).show();
            break;

          case LISTENING:
            setEnabledLook(m_btnAction, false);
            setEnabledLook(m_btnStop, true);
            m_TextViewState.setText("Say something for transcription to start...");
            Toast.makeText(getApplicationContext(),
                           "Recording Started",
                           Toast.LENGTH_LONG).show();
            break;

          default:
            Log.e(m_strClassTag, "Unsupported UI state: " + eUIState);
        }
      }
    });
  }

  public void StopSpeechActivities()
  {
    if (m_sRMEAVClient != null)
    {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "StopSpeechActivities: Client/Server Calling EndEngineInstance");

      m_sRMEAVClient.EndEngineInstance(m_hRMEAVEngineHandle);
    }

    if (m_sRMEAVLocal != null)
    {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "StopSpeechActivities: Local Calling EndEngineInstance");

      m_sRMEAVLocal.EndEngineInstance(m_hRMEAVEngineHandle);
      m_hRMEAVEngineHandle = 0;
    }
  }

  public synchronized void logToConsole(final String strText)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        appendToConsole(strText);
      }
    });
  }

  private void appendToConsole(String strText)
  {
    if (strText == null || m_TextViewConsole == null)
      return;
    Log.i(m_strClassTag, "Console: " + strText);
    m_TextViewConsole.append(strText + "\n");
    m_ScrollView.post(new Runnable()
                     {
                       @Override
                       public void run()
                       {
                         m_ScrollView.fullScroll(View.FOCUS_DOWN);
                       }
                     }
                );
  }

  public void ClearScreen()
  {
    if (m_TextViewConsole == null)
      return;

    m_TextViewConsole.setText("");
    m_ScrollView.post(new Runnable()
                     {
                       @Override
                       public void run()
                       {
                         m_ScrollView.fullScroll(View.FOCUS_DOWN);
                       }
                     }
                );
  }

  public void OutputToScreen(String strText)
  {
    if (m_TextViewConsole == null)
      return;

    m_TextViewConsole.setText(strText);
    m_ScrollView.post(new Runnable()
                     {
                       @Override
                       public void run()
                       {
                         m_ScrollView.fullScroll(View.FOCUS_DOWN);
                       }
                     }
                );
  }


  /********************************************************/
  // Shared Preference Retrieval Functions
  /********************************************************/
  
  public String GetSettingString(String strKey, String strDefault)
  {
    SharedPreferences sharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(this);
    String strString = sharedPreferences.getString(strKey, strDefault);

    return strString;
  }


  public boolean GetSettingBoolean(String strKey, boolean bDefault)
  {
    SharedPreferences sharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(this);

    return sharedPreferences.getBoolean(strKey, bDefault);
  }


  public int GetSettingInt(String strKey, int iDefault)
  {
    /*
    SharedPreferences sharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(this);

    return sharedPreferences.getInt(strKey, iDefault);
    */
    return Integer.parseInt(GetSettingString(strKey, Integer.toString(iDefault)));
  }

  public int GetSettingIntBasic(String strKey, int iDefault)
  {
    SharedPreferences sharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(this);

    return sharedPreferences.getInt(strKey, iDefault);
  }

  public float GetSettingFloat(String strKey, float fDefault)
  {
    /*
    SharedPreferences sharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(this);
    */
    return Float.parseFloat(GetSettingString(strKey, Float.toString(fDefault)));
  }


  public boolean FileExists(String strPath)
  {
    File f = new File(strPath);
    if(f.exists())
      return true;
    else
      return false;
  }


  public boolean DirectoryExists(String strPath)
  {
    File f = new File(strPath);
    if(f.exists() && f.isDirectory())
      return true;
    else
      return false;
  }

  public void SetupSpinnerAction()
  {
    if (m_sRMEAVLocal == null)
      m_listRMEAVAction = m_sRMEAVClient.getValidActions();
    else
      m_listRMEAVAction = m_sRMEAVLocal.getValidActions();

    ArrayAdapter<RMEAVAction> adapterAction =
      new ArrayAdapter<RMEAVAction>(this,
                                    android.R.layout.simple_spinner_item,
                                    m_listRMEAVAction);
    adapterAction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    m_spinnerAction.setAdapter(adapterAction);
  }

  public void DialogEnrollGetClaimedID()
  {
    DialogEnrollClaimedID sDialogEnrollClaimedID = new DialogEnrollClaimedID();
    sDialogEnrollClaimedID.show(getSupportFragmentManager(), "Enter Enroll Claimed ID");
  }

  public void DialogUnenrollGetClaimedID()
  {
    DialogUnenrollClaimedID sDialogUnenrollClaimedID = new DialogUnenrollClaimedID();
    sDialogUnenrollClaimedID.show(getSupportFragmentManager(), "Enter Unenroll Claimed ID");
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults)
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length == 0 ||
        grantResults[0] == PackageManager.PERMISSION_DENIED)
    {
    }
    else
    {
      if (m_iISCDebug > 0)
        Log.d(m_strClassTag, "Permissions fine!");
    }
  }

  // This is the onTaskCompleted that overrides the one in RMEAVListener
  // One way is to use it to Release the API -- Homayoon Beigi 5/8/2020
  @Override
  public void onTaskCompleted(String strResult)
  {
    if (m_sRMEAVLocal != null)
    {
      String strResultRelease;
      strResultRelease = m_sRMEAVLocal.AVRelease();
      logToConsole(String.format(Locale.US, "Releasing API:" + strResultRelease));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater sInflater = getMenuInflater();
    sInflater.inflate(R.menu.settings, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item)
  {
    Intent intent = null;
    switch (item.getItemId())
    {
      case id.action_settings:
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;

        /*
      case R.id.action_speaker_enroll:
        intent = new Intent(this, SpeakerEnrollActivity.class);
        startActivityForResult(intent, RMEAVSettings.REQUEST_SPEAKER_ENROLLMENT_DATA);
        return true;
        */
        
      case id.action_about:
        intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RMEAVSettings.REQUEST_SPEAKER_ENROLLMENT_DATA)
    {
      if (resultCode == RESULT_OK)
      {
        String strSpeakerID = data.getStringExtra("SpeakerID");
        String strSpeakerName = data.getStringExtra("SpeakerName");
        Toast.makeText(getApplicationContext(),
                       "SpeakerID: " + strSpeakerID +
                       " SpeakerName: "+ strSpeakerName,
                        Toast.LENGTH_LONG).show();
      }
      else if (resultCode == RESULT_CANCELED)
      {
        Toast.makeText(getApplicationContext(),
                       "No SpeakerID or SpeakerName were provided!",
                        Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void applyEnrollTexts(String strClaimedID,
                               String strClassDescription,
                               Boolean bEnrollSpeaker,
                               Boolean bEnrollFace)
  {
    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, "Enroll: Setting ClaimedID to " + strClaimedID + " and ClassDescription to " + strClassDescription);

    m_sRMEAVAction.setStrClaimedID(strClaimedID);
    m_sRMEAVAction.setStrClassDescription(strClassDescription);

    m_sRMEAVAction.setStrAction("Enroll");
    ClearScreen();
    if (m_sRMEAVLocal == null)
    {
      String strServerAddress = GetSettingString("pref_EditTextServerAddress", "rmeav1.audiovisualreco.com");
      int iPort = Integer.parseInt(GetSettingString("pref_EditTextServerPort", "17201"));
      String strRMEAVUserID = GetSettingString("pref_EditTextClientServerUserID", "rmeeng16k16");
      String strSpeechLanguage = GetSettingString("pref_ListRMESpchClientServerLanguage", "eng_us");
      String strSpeechModelVersion = GetSettingString("pref_ListRMESpchClientServerModelVersion", "Huge");
      String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceClientServerDetectModelVersion", "Face");
      String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceClientServerRecoModelVersion", "Face");
      int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
      float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);
      float fSpeakerWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpkrWeight", 100) / 100.0);
      float fFaceWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMEFaceWeight", 100) / 100.0);
      Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
      Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
      Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

      m_hRMEAVEngineHandle = m_sRMEAVClient.NewEngineInstance(m_sRMEAVAction,
                                         new RMEAVSettingsManager.Builder()
                                               .setAction("Enroll")
                                               .setClaimedID(strClaimedID)
                                               .setSamplingRate(iSamplingRate)
                                               .setServerAddress(strServerAddress)
                                               .setServerPort(iPort)
                                               .setRMEAVUserID(strRMEAVUserID)
                                               .setSpeechLanguage(strSpeechLanguage)
                                               .setSpeechModelVersion(strSpeechModelVersion)
                                               .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                               .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                               .setSpeechWeight((float) 0.0)
                                               .setSpeakerWeight((float) 1.0)
                                               .setFaceWeight(fFaceWeight)
                                               .setDoEndPointing(bDoEndpointing)
                                               .setDoWordAlignment(bDoWordAlignment)
                                               .setDoPhoneAlignment(bDoPhoneAlignment)
                                               .setDebugLevel(m_iISCDebug)
                                               .build());
    }
    else
    {
      String strRootDir = GetSettingString("pref_EditTextEmbeddedRootDir", "/sdcard/rmeroot");
      String strRMEAVUserID = GetSettingString("pref_EditTextEmbeddedUserID", "rmeeng16k16");
      String strSpeechLanguage = GetSettingString("pref_ListRMESpchEmbeddedLanguage", "eng_us");
      String strSpeechModelVersion = GetSettingString("pref_ListRMESpchEmbeddedModelVersion", "Small");
      String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedDetectModelVersion", "Face");
      String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedRecoModelVersion", "Face");
      int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
      float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);

      float fSpeakerWeight= (float) 0.0;
      if (bEnrollSpeaker)
        fSpeakerWeight = (float) 1.0;

      float fFaceWeight = (float) 0.0;
      if (bEnrollFace)
        fFaceWeight = (float) 1.0;

      if (m_iISCDebug > 1)
      {
        Log.d(m_strClassTag, "Enroll: Setting fSpeakerWeight to " + fSpeechWeight);
        Log.d(m_strClassTag, "Enroll: Setting fFaceWeight to " + fFaceWeight);
      }

      Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
      Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
      Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

      m_hRMEAVEngineHandle = m_sRMEAVLocal.NewEngineInstance(m_sRMEAVAction,
                                                             m_hRMEAVResourceHandle,
                                                             m_sRMEAudioAndroid,
                                                             m_sRMEImageAndroid,
                                          new RMEAVSettingsManager.Builder()
                                                .setAction("Enroll")
                                                .setClaimedID(strClaimedID)
                                                .setSamplingRate(iSamplingRate)
                                                .setRootDir(strRootDir)
                                                .setRMEAVUserID(strRMEAVUserID)
                                                .setSpeechLanguage(strSpeechLanguage)
                                                .setSpeechModelVersion(strSpeechModelVersion)
                                                .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                                .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                                .setSpeechWeight((float) 1.0)
                                                .setSpeakerWeight(fSpeakerWeight)
                                                .setFaceWeight(fFaceWeight)
                                                .setDoEndPointing(bDoEndpointing)
                                                .setDoWordAlignment(bDoWordAlignment)
                                                .setDoPhoneAlignment(bDoPhoneAlignment)
                                                .setDebugLevel(m_iISCDebug)
                                                .build());

     if (m_sRMEAVLocal.EngineStart(m_hRMEAVEngineHandle) == false)
      Log.d(m_strClassTag, "Error Starting the Engine!");
    }
  }

  @Override
  public void applyUnenrollTexts(String strClaimedID,
                                 Boolean bUnenrollSpeaker,
                                 Boolean bUnenrollFace)
  {
    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, "Unenroll: Setting ClaimedID to " + strClaimedID);

    m_sRMEAVAction.setStrClaimedID(strClaimedID);
    m_sRMEAVAction.setStrAction("Unenroll");
    ClearScreen();
    if (m_sRMEAVLocal == null)
    {
      String strServerAddress = GetSettingString("pref_EditTextServerAddress", "rmeav1.audiovisualreco.com");
      int iPort = Integer.parseInt(GetSettingString("pref_EditTextServerPort", "17201"));
      String strRMEAVUserID = GetSettingString("pref_EditTextClientServerUserID", "rmeeng16k16");
      String strSpeechLanguage = GetSettingString("pref_ListRMESpchClientServerLanguage", "eng_us");
      String strSpeechModelVersion = GetSettingString("pref_ListRMESpchClientServerModelVersion", "Huge");
      String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceClientServerDetectModelVersion", "Face");
      String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceClientServerRecoModelVersion", "Face");
      int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
      float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);

      float fSpeakerWeight= (float) 0.0;
      if (bUnenrollSpeaker)
        fSpeakerWeight = (float) 1.0;

      float fFaceWeight = (float) 0.0;
      if (bUnenrollFace)
        fFaceWeight = (float) 1.0;

      if (m_iISCDebug > 1)
      {
        Log.d(m_strClassTag, "Unenroll: Setting fSpeakerWeight to " + fSpeechWeight);
        Log.d(m_strClassTag, "Unenroll: Setting fFaceWeight to " + fFaceWeight);
      }

      Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
      Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
      Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

      m_hRMEAVEngineHandle = m_sRMEAVClient.NewEngineInstance(m_sRMEAVAction,
                                           new RMEAVSettingsManager.Builder()
                                                 .setAction("Unenroll")
                                                 .setClaimedID(strClaimedID)
                                                 .setSamplingRate(iSamplingRate)
                                                 .setServerAddress(strServerAddress)
                                                 .setServerPort(iPort)
                                                 .setRMEAVUserID(strRMEAVUserID)
                                                 .setSpeechLanguage(strSpeechLanguage)
                                                 .setSpeechModelVersion(strSpeechModelVersion)
                                                 .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                                 .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                                 .setSpeechWeight((float) 1.0)
                                                 .setSpeakerWeight((float) 1.0)
                                                 .setFaceWeight(fFaceWeight)
                                                 .setDoEndPointing(bDoEndpointing)
                                                 .setDoWordAlignment(bDoWordAlignment)
                                                 .setDoPhoneAlignment(bDoPhoneAlignment)
                                                 .setDebugLevel(m_iISCDebug)
                                                 .build());
    }
    else
    {
      String strRootDir = GetSettingString("pref_EditTextEmbeddedRootDir", "/sdcard/rmeroot");
      String strRMEAVUserID = GetSettingString("pref_EditTextEmbeddedUserID", "rmeeng16k16");
      String strSpeechLanguage = GetSettingString("pref_ListRMESpchEmbeddedLanguage", "eng_us");
      String strSpeechModelVersion = GetSettingString("pref_ListRMESpchEmbeddedModelVersion", "Small");
      String strFaceDetectModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedDetectModelVersion", "Face");
      String strFaceRecoModelVersion = GetSettingString("pref_ListRMEFaceEmbeddedRecoModelVersion", "Face");
      int iSamplingRate = GetSettingInt("pref_ListRMESpchSamplingRate", 16000);
      float fSpeechWeight = (float) ((float) GetSettingIntBasic("pref_SeekBarRMESpchWeight", 100) / 100.0);

      float fSpeakerWeight= (float) 0.0;
      if (bUnenrollSpeaker)
        fSpeakerWeight = (float) 1.0;

      float fFaceWeight = (float) 0.0;
      if (bUnenrollFace)
        fFaceWeight = (float) 1.0;

      if (m_iISCDebug > 1)
      {
        Log.d(m_strClassTag, "Unenroll: Setting fSpeakerWeight to " + fSpeechWeight);
        Log.d(m_strClassTag, "Unenroll: Setting fFaceWeight to " + fFaceWeight);
      }

      Boolean bDoEndpointing = GetSettingBoolean("pref_SwitchEmbeddedDoEndpointing", true);
      Boolean bDoWordAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoWordAlignment", false);
      Boolean bDoPhoneAlignment = GetSettingBoolean("pref_SwitchEmbeddedDoPhoneAlignment", false);

      m_hRMEAVEngineHandle = m_sRMEAVLocal.NewEngineInstance(m_sRMEAVAction,
                                                             m_hRMEAVResourceHandle,
                                                             m_sRMEAudioAndroid,
                                                             m_sRMEImageAndroid,
                                            new RMEAVSettingsManager.Builder()
                                                  .setAction("Unenroll")
                                                  .setClaimedID(strClaimedID)
                                                  .setSamplingRate(iSamplingRate)
                                                  .setRootDir(strRootDir)
                                                  .setRMEAVUserID(strRMEAVUserID)
                                                  .setSpeechLanguage(strSpeechLanguage)
                                                  .setSpeechModelVersion(strSpeechModelVersion)
                                                  .setFaceDetectModelVersion(strFaceDetectModelVersion)
                                                  .setFaceRecoModelVersion(strFaceRecoModelVersion)
                                                  .setSpeechWeight((float) 1.0)
                                                  .setSpeakerWeight(fSpeakerWeight)
                                                  .setFaceWeight(fFaceWeight)
                                                  .setDoEndPointing(bDoEndpointing)
                                                  .setDoWordAlignment(bDoWordAlignment)
                                                  .setDoPhoneAlignment(bDoPhoneAlignment)
                                                  .setDebugLevel(m_iISCDebug)
                                                  .build());
     if (m_sRMEAVLocal.EngineStart(m_hRMEAVEngineHandle) == false)
       Log.d(m_strClassTag, "Error Starting the Engine!");
    }
  }

  public void sleep(int time)
  {
    try
    {
      Thread.sleep(time);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void onCaptureImage(long hRMEAVEngineHandle,
                             String strImageFormat,
                             int iWidthRequested,
                             int iHeightRequested,
                             int iNumChannelRequested,
                             int iNumBytePerChannelRequested)
  {
    byte[] abyBuffer = null;
    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, "Capturing Image");

    if (m_sRMEImageAndroid != null && m_hRMEAVEngineHandle > 0)
      m_sRMECameraAndroid.CaptureImage(hRMEAVEngineHandle,
                                       strImageFormat,
                                       iWidthRequested,
                                       iHeightRequested,
                                       iNumChannelRequested,
                                       iNumBytePerChannelRequested);

    return;
  }

  
  @Override
  public void onMessageEvent(long hRMEAVEngineHandle,
                             String strResult)
  {
    Log.d(m_strClassTag, strResult);
    try
    {
      ParseResults(hRMEAVEngineHandle, strResult);
    }
    catch (Exception e)
    {
      //Log.e(m_strClassTag, "" + e);
    }
  }

  public void ParseResults(long hRMEAVEngineHandle,
                           final String strMessage) throws Exception
  {
    if (strMessage==null || strMessage.isEmpty())
      return;
    
    try
    {
      if (m_iISCDebug > 1)
        Log.d("RMEAVLocal::ParseResults: ", "Parsing the following message:" + strMessage);

      JSONObject jsonMsg = new JSONObject(strMessage);
      if (jsonMsg == null)
        return;
      
      if (jsonMsg.has("RecoMadeEasy"))
      {
        if (m_iISCDebug > 0)
          Log.d("RMEAVLocal::ParseResults: Final", "TEXT:" + strMessage);
        JSONObject recoJsonObj = jsonMsg.getJSONObject("RecoMadeEasy");

        if (recoJsonObj.has("@Comment"))
        {
          String strComment = recoJsonObj.getString("@Comment");
          if (strComment.equals("Intermediate"))
          {
            if (recoJsonObj.has("MediaLength"))
            {
              if (m_iISCDebug > 1)
                Log.d("RMEAVLocal::ParseResults: Intermediate", "Processing:" + strMessage);
              JSONObject JSONObjectMediaLength = recoJsonObj.getJSONObject("MediaLength");
              String strAudioLengthText = JSONObjectMediaLength.getString("#text");
              String strUnitText = JSONObjectMediaLength.getString("@Unit");
              OutputToScreen(strAudioLengthText+" "+strUnitText);
            }
          }
        }

        if (recoJsonObj.has("IdentifyResult"))
        {
          String strTempString;
          String strSerialNum = recoJsonObj.getString("SerialNum");

          JSONArray IDList = recoJsonObj.getJSONArray("IdentifyResult");
          for (int iID=0; iID<IDList.length(); iID++)
          {
            JSONObject segmentParentJsonObj = IDList.getJSONObject(iID);
            JSONObject segmentJsonObj = segmentParentJsonObj.getJSONObject("Segment");
            int iSegmentIndex = 0;
            int iCornerX = 0;
            int iCornerY = 0;
            int iWidth = 0;
            int iHeight = 0;
            String strID = "";
            float fScore = (float) 0.0;
            float fConfidence = (float) 50.0;
            
            if (segmentJsonObj.has("@Index"))
            {
              String strSegmentIndex = segmentJsonObj.getString("@Index");
              iSegmentIndex = Integer.parseInt(strSegmentIndex);
              Log.d("RMEAVLocal::ParseResults", "Index: " + iSegmentIndex);
            }
            if (segmentJsonObj.has("Location"))
            {
              JSONObject locationJsonObj = segmentJsonObj.getJSONObject("Location");
              strTempString = locationJsonObj.getString("corner_x");
              iCornerX = Integer.parseInt(strTempString);
              strTempString = locationJsonObj.getString("corner_y");
              iCornerY = Integer.parseInt(strTempString);
              strTempString = locationJsonObj.getString("width");
              iWidth = Integer.parseInt(strTempString);
              strTempString = locationJsonObj.getString("height");
              iHeight = Integer.parseInt(strTempString);
            
              Log.d("RMEAVLocal::ParseResults", "" + iCornerX + " " + iCornerY + " " + iWidth + " " + iHeight);
            }
            if (segmentJsonObj.has("TopMatch"))
            {
              JSONObject matchJsonObj = segmentJsonObj.getJSONObject("TopMatch");
              JSONObject scoreJsonObj = matchJsonObj.getJSONObject("Score");
              strTempString = scoreJsonObj.getString("#text");
              fScore = Float.parseFloat(strTempString);
              strID = matchJsonObj.getString("ID");
              strTempString = matchJsonObj.getString("Confidence");
              fConfidence = Float.parseFloat(strTempString);
            }

            String strOutputText = String.format(Locale.US, "Face: %s (%d): %s %3d,%3d %3dx%3d S: %.2f%% %.2f%%\n",
                                                 strSerialNum,
                                                 iSegmentIndex,
                                                 strID,
                                                 iCornerX, iCornerY, iWidth, iHeight,
                                                 fScore, fConfidence);
            logToConsole(strOutputText);
            //            OutputToScreen(strOutputText);
          }
        }

        if (recoJsonObj.has("result"))
        {
          JSONObject resultJsonObj = recoJsonObj.getJSONObject("result");
          JSONArray hypothesisList = resultJsonObj.getJSONArray("hypotheses");
          JSONObject topResult = hypothesisList.getJSONObject(0);
          String topResultString = topResult.getString("transcript");
          m_strTranscript = topResultString;
          if (m_iISCDebug > 1)
            logToConsole(String.format(Locale.US, "\nTRANSCRIPT: \"" + m_strTranscript +"\""));
          else
            logToConsole(String.format(Locale.US, m_strTranscript + ".\n"));
          for (int i = 0; i < hypothesisList.length(); i++)
          {
            JSONObject iVal = hypothesisList.getJSONObject(i);
            if (m_iISCDebug > 1)
              Log.d("RMEAVLocal::ParseResults", "" + i + ":" + iVal.toString());
          }

          if (m_bStopSpeechAutomatically == true)
          {
            if (m_enumUIState == RMEUIState.LISTENING)
            {
              // Stop Speech after each final result
              setUIState(RMEUIState.NOT_LISTENING);
              StopSpeechActivities();
            }
          }
        }
        // Speaker results
        else if (recoJsonObj.has("Segment"))
        {
          if (m_iISCDebug > 0)
            Log.d("RMEAVLocal::ParseResults", "Parsing Speaker Reco Results");
          JSONObject segmentJsonObj = recoJsonObj.getJSONObject("Segment");
          String strSegmentIndex = segmentJsonObj.getString("@Index");
          int iSegmentIndex = Integer.parseInt(strSegmentIndex);
          JSONObject JSONObjectTimeStampBegin = segmentJsonObj.getJSONObject("TimeStampBegin");
          String strTimeStampBeginText = JSONObjectTimeStampBegin.getString("#text");
          float fSegmentTimeStampBegin = Float.parseFloat(strTimeStampBeginText);
          JSONObject JSONObjectTimeStampEnd = segmentJsonObj.getJSONObject("TimeStampEnd");
          String strTimeStampEndText = JSONObjectTimeStampEnd.getString("#text");
          float fSegmentTimeStampEnd = Float.parseFloat(strTimeStampEndText);
          
          if (segmentJsonObj.has("TopMatch"))
          {
            JSONObject JSONObjectTopMatch = segmentJsonObj.getJSONObject("TopMatch");
            String strTopMatchID = JSONObjectTopMatch.getString("ID");
            String strTopMatchClassDescription = JSONObjectTopMatch.getString("ClassDescription");
            JSONObject JSONObjectScore = JSONObjectTopMatch.getJSONObject("Score");
            String strScoreText = JSONObjectScore.getString("#text");
            float fScore = Float.parseFloat(strScoreText);

            logToConsole(String.format(Locale.US, "Speaker: Seg " + iSegmentIndex + ": " +
                                                  fSegmentTimeStampBegin + "s" +
                                                  " to " + + fSegmentTimeStampEnd + "s: " +
                                                  strTopMatchID + "("+fScore+")"));
          }
          else
          {
            logToConsole(String.format(Locale.US, "Speaker: Seg " + iSegmentIndex + ": " +
                                                  fSegmentTimeStampBegin + "s" +
                                                  " to " + fSegmentTimeStampEnd + "s"));
          }
        }
        else if (recoJsonObj.has("Enrollment"))
        {
          if (m_iISCDebug > 0)
            Log.d("RMEAVLocal::ParseResults", "Parsing Spkr Enrollment Results");
          JSONObject JsonObjectEnrollment = recoJsonObj.getJSONObject("Enrollment");
          String strEnrollmentClaimedID = "";
          String strEnrollmentClaimedDescription = "";
          String strEnrollmentEnrollStatus = "";
          String strEnrollmentMessage = "";
          if (JsonObjectEnrollment.has("ClaimedID"))
            strEnrollmentClaimedID = JsonObjectEnrollment.getString("ClaimedID");
          if (JsonObjectEnrollment.has("ClaimedDescription"))
            strEnrollmentClaimedDescription = JsonObjectEnrollment.getString("ClaimedDescription");
          if (JsonObjectEnrollment.has("EnrollStatus"))
            strEnrollmentEnrollStatus = JsonObjectEnrollment.getString("EnrollStatus");
          if (JsonObjectEnrollment.has("Message"))
            strEnrollmentMessage = JsonObjectEnrollment.getString("Message");
          
          if (m_iISCDebug > 0)
            Log.d("RMEAVLocal::ParseResults", "strEnrollmentEnrollStatus is \""+strEnrollmentEnrollStatus+"\"");

          if (strEnrollmentEnrollStatus.equals("Enrolled"))
            logToConsole(String.format(Locale.US, "\nSpeaker " + strEnrollmentClaimedID +
                                                  " (" + strEnrollmentClaimedDescription + ")" +
                                                  " was " + strEnrollmentEnrollStatus +
                                                  "\n" + strEnrollmentMessage));
          else
          {
            logToConsole(String.format(Locale.US, "Speaker " + strEnrollmentClaimedID +
                                                  " (" + strEnrollmentClaimedDescription + ")" +
                                                  " is " + strEnrollmentEnrollStatus +
                                                  "\n" + strEnrollmentMessage));
            if (m_enumUIState == RMEUIState.LISTENING)
            {
              setUIState(RMEUIState.NOT_LISTENING);
              StopSpeechActivities();
            }
          }
        }
      }
      else if (jsonMsg.has("adaptation_state"))
      {
        if (m_iISCDebug > 0)
          Log.d("RMEAVLocal::ParseResults:", "TEXT:" + strMessage);
        if (m_iISCDebug > 1)
          logToConsole(String.format(Locale.US, "\n" + strMessage));
      }
      else if (jsonMsg.has("status"))
      {
        if (m_iISCDebug > 0)
          Log.d("RMEAVLocal::ParseResults: Intermediate", "TEXT:" + strMessage);
        JSONObject resultJsonObj = jsonMsg.getJSONObject("result");
        JSONArray hypothesisList = resultJsonObj.getJSONArray("hypotheses");
        JSONObject topResult = hypothesisList.getJSONObject(0);
        String topResultString = topResult.getString("transcript");
        if (m_iISCDebug > 1)
          logToConsole(String.format(Locale.US, "\nTRANSCRIPT: \"" + topResultString +"\""));
        else
        {
          PostToToast(topResultString, this);
       }
      }
      else if (jsonMsg.has("frame"))
      {
        if (m_iISCDebug > 0)
          Log.d("RMEAVLocal::ParseResults: Image Results", "TEXT:" + strMessage);
        JSONObject frameJsonObj = jsonMsg.getJSONObject("frame");
        JSONArray hypothesisList = frameJsonObj.getJSONArray("objects");
        JSONObject topResult = hypothesisList.getJSONObject(0);
        String topResultString = topResult.getString("name");
        if (m_iISCDebug > 1)
          logToConsole(String.format(Locale.US, "\nImage Detection: \"" + topResultString +"\""));
        else
        {
          PostToToast(topResultString, this);
       }
      }
      else
      {
        if (m_iISCDebug > 0)
          Log.e("RMEAVLocal::ParseResults", "Intermediate Results:" + strMessage);
        if (m_iISCDebug > 0)
          logToConsole(String.format(Locale.US, "\n" + strMessage));
      }
    }
    catch (Exception e)
    {
      Log.e("RMEAVLocal::ParseResults", "CATCH:" + e);
      logToConsole(String.format(Locale.US, "\n" + strMessage));
    }
  }

  public synchronized void PostToToast(String strString, final MainActivity sUI)
  {
    sUI.runOnUiThread(new Runnable()
                      {
                        @Override
                        public void run()
                        {
                          Toast.makeText(sUI,
                                         strString,
                                         Toast.LENGTH_LONG).show();
                        }
                      });
  }
}
