package com.recotechnologies.rmeavdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;


import java.util.Locale;


public class AboutActivity extends AppCompatActivity
{
  private final String m_strClassTag = "RMEAVAbout";
  private TextView m_TextViewAbout = null;
  private ScrollView m_ScrollViewAbout = null;

  String m_strAboutHTMLText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        m_strAboutHTMLText = getString(R.string.about_html_text);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("About");
        getSupportActionBar().setIcon(getDrawable(R.mipmap.speechsignal_short));

        m_ScrollViewAbout = (ScrollView) findViewById(R.id.scrollViewAbout);
        m_TextViewAbout = (TextView) findViewById(R.id.textViewAbout);
        logToConsole(String.format(Locale.US, m_strAboutHTMLText));
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
    if (strHTMLText == null || m_TextViewAbout == null)
      return;
    Log.i(m_strClassTag, "About: " + strHTMLText);

    m_TextViewAbout.setMovementMethod(LinkMovementMethod.getInstance());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
      m_TextViewAbout.setText(Html.fromHtml(strHTMLText, Html.FROM_HTML_MODE_COMPACT));
    else
      m_TextViewAbout.setText(Html.fromHtml(strHTMLText));

    //m_TextViewAbout.append("\n");
    m_ScrollViewAbout.post(new Runnable()
                     {
                       @Override
                       public void run()
                       {
                         m_ScrollViewAbout.fullScroll(View.FOCUS_DOWN);
                       }
                     }
                );
  }

}
