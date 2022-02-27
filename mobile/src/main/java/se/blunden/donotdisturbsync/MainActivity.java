/*
 * Copyright (C) 2017-2022 blunden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.blunden.donotdisturbsync;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DndSync";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateDNDPermissionStatus();

        Button permissionButton = findViewById(R.id.button_permission);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPermissionGrantingSettings();
            }
        });
    }

    private void hideLauncherIcon() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void showPermissionGrantingSettings() {
        Log.i(TAG, "Launching permissions settings activity on the device");

        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);

        try {
            // Some devices may not have this activity it seems
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showNeutralDialog(R.string.error_no_permission_activity);

            Log.e(TAG, "Failed to open the Do Not Disturb access settings");
        }
    }

    private void showNeutralDialog(@androidx.annotation.StringRes int messageResId) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(messageResId)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        dialog.show();
    }

    private void updateDNDPermissionStatus() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Button permissionButton = findViewById(R.id.button_permission);
        TextView permissionStatusText = findViewById(R.id.permission_status_text);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            permissionButton.setVisibility(View.GONE);
            permissionStatusText.setVisibility(View.VISIBLE);
        } else {
            permissionButton.setVisibility(View.VISIBLE);
            permissionStatusText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the items to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent launchSettings = new Intent(this, SettingsActivity.class);
                startActivity(launchSettings);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
