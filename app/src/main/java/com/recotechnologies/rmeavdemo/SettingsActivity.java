package com.recotechnologies.rmeavdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.recotechnologies.rmeavdemo.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity
{
  private final String m_strClassTag = "RMEAVSettings";
  private TextView m_TextViewSettings = null;
  private ScrollView m_ScrollViewSettings = null;

  String m_strSettingsText = "This is the settings page";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setTitle("Settings");
    getSupportActionBar().setIcon(getDrawable(R.mipmap.speechsignal_short));

    m_ScrollViewSettings = (ScrollView) findViewById(R.id.scrollViewSettings);
    if (findViewById(R.id.SettingsPreferencesFragmentContainer) != null)
    {
      if (savedInstanceState != null)
        return;

      getSupportFragmentManager().beginTransaction().add(R.id.SettingsPreferencesFragmentContainer, new SettingsPreferencesFragment()).commit();
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
    if (strText == null || m_TextViewSettings == null)
      return;
    Log.i(m_strClassTag, "Settings: " + strText);
    m_TextViewSettings.append(strText + "\n");
    m_ScrollViewSettings.post(new Runnable()
                     {
                       @Override
                       public void run()
                       {
                         m_ScrollViewSettings.fullScroll(View.FOCUS_DOWN);
                       }
                     }
                );
  }

}
