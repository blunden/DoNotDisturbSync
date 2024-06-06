/*
 * Copyright (C) 2017 blunden
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

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.splashscreen.SplashScreen;
import androidx.wear.activity.ConfirmationActivity;
import androidx.wear.remote.interactions.RemoteActivityHelper;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String TAG = "DndSync";
    private static final float FACTOR = 0.146467f; // From BoxInsetLayout ((1 - sqrt(2)/2)/2)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton showInstructionsButton = findViewById(R.id.button_show_instructions);
        showInstructionsButton.setOnClickListener(view -> {
            Intent showInstructionsIntent = new Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("https://github.com/blunden/DoNotDisturbSync/blob/master/README.md"));

            Executor executor = Executors.newSingleThreadExecutor();
            RemoteActivityHelper remoteActivityHelper = new RemoteActivityHelper(this, executor);
            ListenableFuture<Void> result = remoteActivityHelper.startRemoteActivity(showInstructionsIntent);

            // Should really be handled by Futures.addCallback(...) but the onSuccess or onFailure
            // callbacks are seemingly never executed. Let's just assume it was successful to show
            // the animation and message.
            result.addListener(() -> {
                try {
                    result.get();
                } catch(Exception e) {
                    Toast.makeText(this, "Failed to open link on companion phone", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to open link on companion phone");
                }
            }, executor);

            Intent confirmationIntent = new Intent(getApplicationContext(), ConfirmationActivity.class)
                    .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                    .putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.instruction_continue_on_phone));
            try {
                startActivity(confirmationIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open link on companion phone", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to start confirmation activity");
            }
        });

        // Adjust insets for round watches
        adjustInsets();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatus();
    }

    private void updatePermissionStatus() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        TextView activityInfoText = findViewById(R.id.info_text);
        MaterialButton showInstructionsButton = findViewById(R.id.button_show_instructions);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            showInstructionsButton.setVisibility(View.GONE);
            activityInfoText.setText(R.string.activity_info_permission_granted);
        }
    }

    private void adjustInsets() {
        if (getResources().getConfiguration().isScreenRound()) {
            int inset = (int) (FACTOR * Resources.getSystem().getDisplayMetrics().widthPixels);
            findViewById(R.id.watch_content_view).setPadding(inset, inset, inset, inset);
        }
    }
}
