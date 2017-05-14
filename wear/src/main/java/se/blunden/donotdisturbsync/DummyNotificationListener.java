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

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * A dummy {@link NotificationListenerService} service that disables and stops itself.
 *
 * Its purpose is to allow users to add the app as a notification listener which
 * automatically grants and enables android.permission.ACCESS_NOTIFICATION_POLICY
 * that can't be enabled the normal way on Android Wear 2.0.
 */
public class DummyNotificationListener extends NotificationListenerService {
    private static final String TAG = "DndDummyService";

    @Override
    public void onListenerConnected() {
        // We don't want to run a background service so disable and stop it to
        // to avoid running this service in the background
        disableServiceComponent();
        Log.i(TAG, "Disabling service");

        try {
            stopSelf();
        } catch(SecurityException e) {
            Log.e(TAG, "Failed to stop service");
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Override this method to be explicit that we don't eavesdrop on notifications
        // even if users have enabled the
    }

    private void disableServiceComponent() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, DummyNotificationListener.class);
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
