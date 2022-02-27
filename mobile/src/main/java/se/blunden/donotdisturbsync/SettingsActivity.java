package se.blunden.donotdisturbsync;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "DNDSyncSettings";

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

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from XML
            addPreferencesFromResource(R.xml.preferences);

            final Preference preferredDndMode = findPreference("preferred_phone_dnd_mode");
            preferredDndMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String newModeName = "Unknown";
                    String[] dndModeNames = getResources().getStringArray(R.array.dnd_mode_names);
                    List<String> dndModeValues = Arrays.asList(getResources().getStringArray(R.array.dnd_mode_values));
                    int dndModeNameIndex = dndModeValues.indexOf((String) newValue);
                    if (dndModeNameIndex != -1) {
                        newModeName = dndModeNames[dndModeNameIndex];
                    }
                    preferredDndMode.setSummary(String.format(getString(R.string.settings_preferred_dnd_mode_summary), newModeName));

                    return true;
                }
            });

            final Preference dndPermissionStatus = findPreference("dnd_permission_status");

            if (isDNDPermissionGranted()) {
                dndPermissionStatus.setSummary(R.string.settings_dnd_permission_granted);
            } else {
                dndPermissionStatus.setSummary(R.string.settings_dnd_permission_denied);
            }

            dndPermissionStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!isDNDPermissionGranted()) {
                        Log.i(TAG, "Launching permissions settings activity on the device");

                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);

                        try {
                            // Some devices may not have this activity it seems
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "Failed to open the Do Not Disturb access settings");
                        }

                        return true;
                    } else {
                        Toast.makeText(getActivity(), R.string.settings_dnd_permission_granted_toast, Toast.LENGTH_SHORT).show();

                        return true;
                    }
                }
            });
        }

        private boolean isDNDPermissionGranted() {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        }
    }
}
