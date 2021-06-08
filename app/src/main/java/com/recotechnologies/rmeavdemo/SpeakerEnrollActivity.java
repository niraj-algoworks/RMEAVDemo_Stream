package com.recotechnologies.rmeavdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.recotechnologies.rmeavapi.RMEAVLocal;
import com.recotechnologies.rmeavdemo.R;

import java.util.Locale;


public class SpeakerEnrollActivity extends AppCompatActivity
{
  private final String m_strClassTag = "RMEAVSpeakerEnroll";
  private TextView m_TextViewSpeakerEnroll = null;
  private ScrollView m_ScrollViewSpeakerEnroll = null;

  public RMEAVLocal m_sRMEAVLocal = null;

  String m_strSpeakerEnrollHTMLText = null;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_speaker_enroll);

    m_strSpeakerEnrollHTMLText = getString(R.string.speaker_enroll_html_text);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setTitle("Speaker Enrollment");
    getSupportActionBar().setIcon(getDrawable(R.mipmap.speechsignal_short));

    m_ScrollViewSpeakerEnroll = (ScrollView) findViewById(R.id.scrollViewSpeakerEnroll);
    m_TextViewSpeakerEnroll = (TextView) findViewById(R.id.textViewSpeakerEnroll);
    logToConsole(String.format(Locale.US, m_strSpeakerEnrollHTMLText));
  }

  public synchronized void logToConsole(final String strHTMLText)
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        appendToConsole(strHTMLText);
      }
    });
  }

  private void appendToConsole(String strHTMLText)
  {
    if (strHTMLText == null || m_TextViewSpeakerEnroll == null)
      return;

    Log.i(m_strClassTag, "Please read the following text:\n" + strHTMLText);

    m_TextViewSpeakerEnroll.setMovementMethod(LinkMovementMethod.getInstance());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
      m_TextViewSpeakerEnroll.setText(Html.fromHtml(strHTMLText, Html.FROM_HTML_MODE_COMPACT));
    else
      m_TextViewSpeakerEnroll.setText(Html.fromHtml(strHTMLText));

    //m_TextViewSpeakerEnroll.append("\n");
    m_ScrollViewSpeakerEnroll.post(new Runnable()
                                   {
                                     @Override
                                     public void run()
                                     {
                                       m_ScrollViewSpeakerEnroll.fullScroll(View.FOCUS_DOWN);
                                     }
                                   }
                                  );
  }

  public void RMESpkrEnroll(View view)
  {
    TextInputEditText sTextInputSpeakerID = findViewById(R.id.textInputSpeakerID);
    TextInputEditText sTextInputSpeakerName = findViewById(R.id.textInputSpeakerName);

    String strSpeakerID  = sTextInputSpeakerID.getText().toString().trim();
    String strSpeakerName  = sTextInputSpeakerName.getText().toString().trim();
    Intent intentEnrollmentData = new Intent();
    intentEnrollmentData.putExtra("SpeakerID", strSpeakerID);
    intentEnrollmentData.putExtra("SpeakerName", strSpeakerName);
    setResult(RESULT_OK, intentEnrollmentData);
    finish();
  }
}
