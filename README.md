Do Not Disturb Sync for Wear
============

Android Wear 2.0 removed synchronization of the Do Not Disturb
state between the watch and the phone. This application restores
the Android Wear 1.5 behaviour with minimal or no battery life impact.

Version 1.1 of the app optionally adds support for full bi-directional
sync. To enable phone to watch sync, users need to manually set the app
as a notification listener **on the watch** by running the following commands:

## Wear OS 2.x/3.x instructions (credit to rhaeus on XDA for finding the new command):

```sh
$ adb shell cmd notification allow_listener se.blunden.donotdisturbsync/se.blunden.donotdisturbsync.DummyNotificationListener
```

## Android Wear 2.0 instructions:

```sh
$ adb shell

$ settings put secure enabled_notification_listeners com.google.android.wearable.app/com.google.android.clockwork.stream.NotificationCollectorService:se.blunden.donotdisturbsync/se.blunden.donotdisturbsync.DummyNotificationListener
```

**NOTE (Only applies to Android Wear):** If you have already manually added other notification listeners
on your watch, you need to instead read the current value and then set it
again with *:se.blunden.donotdisturbsync/se.blunden.donotdisturbsync.DummyNotificationListener*
added.

[Play Store](https://play.google.com/store/apps/details?id=se.blunden.donotdisturbsync)
