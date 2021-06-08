package com.recotechnologies.rmeavdemo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class SettingsPreferencesFragment extends PreferenceFragmentCompat
  implements OnSharedPreferenceChangeListener
{
    private SharedPreferences m_sSharedPreferences;
    Bundle m_sSavedInstanceState;
    private SeekBarPreference m_sSeekBarRMESpchWeight;
    private SeekBarPreference m_sSeekBarRMESpkrWeight;
    private SeekBarPreference m_sSeekBarRMEFaceWeight;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings_preferences);
      m_sSavedInstanceState = savedInstanceState;

      m_sSeekBarRMESpchWeight = findPreference("pref_SeekBarRMESpchWeight");
      m_sSeekBarRMESpkrWeight = findPreference("pref_SeekBarRMESpkrWeight");
      m_sSeekBarRMEFaceWeight = findPreference("pref_SeekBarRMEFaceWeight");
    }


  @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
    //  setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }

        @Override
    public void onResume() {
        super.onResume();

        m_sSharedPreferences = getPreferenceManager().getSharedPreferences();

        onSharedPreferenceChanged(m_sSharedPreferences, "pref_SeekBarRMESpchWeight");
        onSharedPreferenceChanged(m_sSharedPreferences, "pref_SeekBarRMESpkrWeight");
        onSharedPreferenceChanged(m_sSharedPreferences, "pref_SeekBarRMEFaceWeight");

        // we want to watch the preference values' changes
        m_sSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        m_sSeekBarRMESpchWeight.setOnPreferenceChangeListener(
                 new Preference.OnPreferenceChangeListener()
                 {
                   @Override
                   public boolean onPreferenceChange(Preference preference, Object newValue)
                   {
                     final int progress = Integer.valueOf(String.valueOf(newValue));
                     preference.setSummary(String.format("%d%%", progress));
                     return true;
                 }
               });

        m_sSeekBarRMESpkrWeight.setOnPreferenceChangeListener(
                 new Preference.OnPreferenceChangeListener()
                 {
                   @Override
                   public boolean onPreferenceChange(Preference preference, Object newValue)
                   {
                     final int progress = Integer.valueOf(String.valueOf(newValue));
                     preference.setSummary(String.format("%d%%", progress));
                     return true;
                 }
               });

        m_sSeekBarRMEFaceWeight.setOnPreferenceChangeListener(
                 new Preference.OnPreferenceChangeListener()
                 {
                   @Override
                   public boolean onPreferenceChange(Preference preference, Object newValue)
                   {
                     final int progress = Integer.valueOf(String.valueOf(newValue));
                     preference.setSummary(String.format("%d%%", progress));
                     return true;
                 }
               });

        Map<String, ?> preferencesMap = m_sSharedPreferences.getAll();

        // iterate through the preference entries and update their summary if they are an instance of EditTextPreference
        for (Map.Entry<String, ?> preferenceEntry : preferencesMap.entrySet())
        {
            if (preferenceEntry instanceof EditTextPreference)
            {
              updateSummary((EditTextPreference) preferenceEntry);
            }
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();
       // m_sSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key)
    {
      Log.d("onSharedPreferenceChanged", key);

      if (key.equals("pref_SeekBarRMESpchWeight"))
      {
        int iValue = sharedPreferences.getInt(key, 100);
        m_sSeekBarRMESpchWeight.setSummary("" + iValue + "%");
      }
      
      if (key.equals("pref_SeekBarRMESpkrWeight"))
      {
        int iValue = sharedPreferences.getInt(key, 100);
        m_sSeekBarRMESpkrWeight.setSummary("" + iValue + "%");
      }
      
      if (key.equals("pref_SeekBarRMEFaceWeight"))
      {
        int iValue = sharedPreferences.getInt(key, 100);
        m_sSeekBarRMEFaceWeight.setSummary("" + iValue + "%");
      }
      

      Map<String, ?> preferencesMap = sharedPreferences.getAll();

      Log.d("Settings", "Shared Preference changed");
      // get the preference that has been changed
      Object changedPreference = preferencesMap.get(key);
      // and if it's an instance of EditTextPreference class, update its summary
      if (preferencesMap.get(key) instanceof EditTextPreference)
      {
          updateSummary((EditTextPreference) changedPreference);
      }

    }

    private void updateSummary(EditTextPreference preference) {
        // set the EditTextPreference's summary value to its current text
        preference.setSummary(preference.getText());
        Log.d("Settings", "getText:"+preference.getText());
    }
}

