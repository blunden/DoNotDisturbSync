package se.blunden.donotdisturbsync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUseRingerModeSetting(boolean useRingerMode) {
        // Build the Intent needed to start the WearMessageSenderService
        Intent wearMessageSenderServiceIntent = new Intent(this, WearMessageSenderService.class);
        wearMessageSenderServiceIntent.setAction(WearMessageSenderService.ACTION_SEND_MESSAGE);
        wearMessageSenderServiceIntent.putExtra(WearMessageSenderService.EXTRA_SETTING_USE_RINGER_MODE,
                String.valueOf(useRingerMode));

        startService(wearMessageSenderServiceIntent);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from XML
            addPreferencesFromResource(R.xml.preferences);

            final Preference useRingerMode = findPreference("use_ringer_mode");
            useRingerMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Activity parentActivity = getActivity();
                    if (parentActivity instanceof SettingsActivity) {
                        ((SettingsActivity) parentActivity).sendUseRingerModeSetting((Boolean) newValue);
                    }

                    return true;
                }
            });
        }
    }
}
