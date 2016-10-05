# PushMessages

Example of Push Messaging feature of Adobe Mobile Services SDK

## Documentation reference

* [Set up a GCM Client App on Android](https://developers.google.com/cloud-messaging/android/client)
* [Adobe Android SDK 4.x Push Messaging](https://marketing.adobe.com/resources/help/en_US/mobile/android/push_messaging.html)
* [Adobe Official Android Simple app](https://github.com/Adobe-Marketing-Cloud/mobile-services/releases/tag/ADBMobileSample-v1.4-Android)

## Create an API project in Firebase console

* Go to : https://console.firebase.google.com/

New Cloud Messaging projects must create a Firebase project in the Firebase console. 

In this process, you'll generate a configuration file and credentials for your project.

1. Create a Firebase project in the Firebase console, if you don't already have one. If you already have an existing Google project associated with your mobile app, click Import Google Project. Otherwise, click Create New Project.

2. Click Add Firebase to your Android app and follow the setup steps. If you're importing an existing Google project, this may happen automatically and you can just download the config file.

3. When prompted, enter your app's package name. It's important to enter the package name your app is using; this can only be set when you add an app to your Firebase project.

4. At the end, you'll download a google-services.json file. You can download this fileagain at any time.

5. If you haven't done so already, copy this into your project's module folder, typically app/.

## Create your app and implement Adobe Mobilse Services Android SDK

To do please follow the steps in the official [documentation](https://marketing.adobe.com/resources/help/en_US/mobile/android/dev_qs.html)
> Make sure to implement lifecycle tracking successfully
>
> Marketing Cloud Visitor ID needs to be enable for push messaging

## GMS depencies

* In Android Studio put project view >> under app copy google-services.json
* Add the following in: **Project-level build.gradle project/build.gradle:**
````
buildscript {
  dependencies {
    // Add this line
    classpath 'com.google.gms:google-services:3.0.0'
  }
}
````
* Click Sync Now
* Add the following in: **App-level build.gradle project/app-module/build.gradle:**
````
// Add to the bottom of the file
apply plugin: 'com.google.gms.google-services'
````
* Add the following: **App-level build.gradle project/app-module/build.gradle:**
````
dependencies {
	    //add this line
	    compile 'com.google.android.gms:play-services-gcm:9.6.1'
}
````
* Add a new Package named messaging
* Create MyInstanceIDListenerService under package messaging
```java
package com.tutorial.alexiscazes.pushmessages.messaging;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}

```
* Add RegistrationIntentService under messaging package
````java
package com.tutorial.alexiscazes.pushmessages.messaging;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.adobe.mobile.Config;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tutorial.alexiscazes.pushmessages.R;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "ADBRegIntentServ";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), /*replace with your sender id*/
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                //send the gcm registration token to the adobemobile sdk
                Config.setPushIdentifier(token);

                GcmPubSub pubSub = GcmPubSub.getInstance(this);
                pubSub.subscribe(token, "/topics/global", null);

                Log.d("", "////////////////////////////////////////");
                Log.d(TAG, "PushID (reg token): " + token);
                Log.d("", "////////////////////////////////////////");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to refresh token", e);
        }
    }
}

````
* Add MyGcmListenerService under messaging package:
````java
package com.tutorial.alexiscazes.pushmessages.messaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.tutorial.alexiscazes.pushmessages.MainActivity;
import com.tutorial.alexiscazes.pushmessages.R;

public class MyGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d("MessageContent", "From: " + from);
        Log.d("MessageContent", "Message: " + message);
        sendNotification(message, data);
    }

    private void sendNotification(String message, Bundle data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //put the data bundle in the intent to track clickthroughs
        intent.putExtras(data);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("ADBMobileSamples")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
````
* Add the following in MainActivity:
````java
Intent registrationIntent = new Intent(this, RegistrationIntentService.class);
        startService(registrationIntent);
````
* Final Code of MainActivity:
````java
package com.tutorial.alexiscazes.pushmessages;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.tutorial.alexiscazes.pushmessages.messaging.RegistrationIntentService;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Map<String, Object> lifecycleData = null;
    private Map<String, Object> acquisitionData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent registrationIntent = new Intent(this, RegistrationIntentService.class);
        startService(registrationIntent);

		/*
		 * Adobe Tracking - Analytics
		 *
		 * set the context for the SDK
		 * this is necessary for access to sharedPreferences and file i/o
		 */
        Config.setContext(this.getApplicationContext());

        /*
		 * Adobe Tracking - Config
		 *
		 * turn on debug logging for the ADBMobile SDK
		 */
        Config.setDebugLogging(true);

		/*
		 * Adobe - Config
		 *
		 * register Callback for Adobe Events
		 */
        Config.registerAdobeDataCallback(new Config.AdobeDataCallback() {
            @Override
            public void call(Config.MobileDataEvent event, Map<String, Object> contextData) {
                String adobeEventTag = "ADOBE_CALLBACK_EVENT";
                switch (event) {
                    case MOBILE_EVENT_LIFECYCLE:
						/* this event will fire when the Adobe sdk finishes processing lifecycle information */
                        lifecycleData = contextData;
                        break;
                    case MOBILE_EVENT_ACQUISITION_INSTALL:
						/* this event will fire on the first launch of the application after install when installed via an Adobe acquisition link */
                        acquisitionData = contextData;
                        break;
                    case MOBILE_EVENT_ACQUISITION_LAUNCH:
						/* this event will fire on the subsequent launches after the application was installed via an Adobe acquisition link */
                        acquisitionData = contextData;
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
		/*
		 * Adobe Tracking - Config
		 *
		 * call pauseCollectingLifecycleData() in case leaving this activity also means leaving the app
		 * must be in the onPause() of every activity in your app
		 */
        Config.pauseCollectingLifecycleData();

    }

    @Override
    protected void onResume() {
        super.onResume();
		/*
		 * Adobe Tracking - Config
		 *
		 * call collectLifecycleData() to begin collecting lifecycle data
		 * must be in the onResume() of every activity in your app
		 */
        Config.collectLifecycleData(this);

		/*
		 * Adobe Tracking - Analytics
		 *
		 * call to trackState(...) for view states report
		 * trackState(...) increments the page view
		 */
        Analytics.trackState("Main Menu push messaging", null);
    }
}

````
* Update AndroidManifest with the following:
````xml
<permission android:name="com.tutorial.alexiscazes.pushmessages.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.tutorial.alexiscazes.pushmessages.permission.C2D_MESSAGE" />
    <!-- [START gcm_permission] -->
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <!-- [END gcm_permission] -->
````

````xml
<receiver android:name="com.adobe.mobile.MessageNotificationHandler" />

<!-- [START gcm_receiver] -->
        <receiver
            android:name="com.google.android.gms.gcm.GcmReceiver"
            android:exported="true"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="gcm.play.android.samples.com.gcmquickstart" />
            </intent-filter>
        </receiver>
        <!-- [END gcm_receiver] -->

        <!-- [START gcm_listener] -->
        <service
            android:name=".messaging.MyGcmListenerService"
            android:exported="false" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            </intent-filter>
        </service>
        <!-- [END gcm_listener] -->

        <!-- [START instanceId_listener] -->
        <service
            android:name=".messaging.MyInstanceIDListenerService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.android.gms.iid.InstanceID"/>
            </intent-filter>
        </service>
        <!-- [END instanceId_listener] -->

        <service
            android:name=".messaging.RegistrationIntentService"
            android:exported="false">
        </service>


````

* Final AndroidManifest:
````xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.tutorial.alexiscazes.pushmessages">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <permission android:name="com.tutorial.alexiscazes.pushmessages.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.tutorial.alexiscazes.pushmessages.permission.C2D_MESSAGE" />
    <!-- [START gcm_permission] -->
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <!-- [END gcm_permission] -->


    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name="com.adobe.mobile.MessageFullScreenActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar" />

        <receiver android:name="com.adobe.mobile.MessageNotificationHandler" />

        <receiver android:name="com.adobe.mobile.ReferralReceiver" android:exported="true">
            <intent-filter>
                <action android:name="com.android.vending.INSTALL_REFERRER" />
            </intent-filter>
        </receiver>

        <!-- [START gcm_receiver] -->
        <receiver
            android:name="com.google.android.gms.gcm.GcmReceiver"
            android:exported="true"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="gcm.play.android.samples.com.gcmquickstart" />
            </intent-filter>
        </receiver>
        <!-- [END gcm_receiver] -->

        <!-- [START gcm_listener] -->
        <service
            android:name=".messaging.MyGcmListenerService"
            android:exported="false" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            </intent-filter>
        </service>
        <!-- [END gcm_listener] -->

        <!-- [START instanceId_listener] -->
        <service
            android:name=".messaging.MyInstanceIDListenerService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.android.gms.iid.InstanceID"/>
            </intent-filter>
        </service>
        <!-- [END instanceId_listener] -->

        <service
            android:name=".messaging.RegistrationIntentService"
            android:exported="false">
        </service>

    </application>




</manifest>
````

## Test

When you tes your app you should see log similar to the following:
> 09-30 16:05:01.310 13398-13452/com.tutorial.alexiscazes.pushmessages D/ADBRegIntentServ: PushID (reg token): dQnwecbrtIU:APA91bGPYUDVI9fh1dBvQB2-t-tFfq-cB-zK0dNtJxolPle1DREX-S-m4Pbe8qAdZfygFBBi1TnxE_QGyiNw2VkGRT9w-HrAxe6S2c6XNVMObumu64iCZDUGOGoIBR3GWbzNFTzu0cpy
09-30 16:05:01.310 13398-13452/com.tutorial.alexiscazes.pushmessages D/ADBRegIntentServ: PushID (reg token): dQnwecbrtIU:APA91bGPYUDVI9fh1dBvQB2-t-tFfq-cB-zK0dNtJxolPle1DREX-S-m4Pbe8qAdZfygFBBi1TnxE_QGyiNw2VkGRT9w-HrAxe6S2c6XNVMObumu64iCZDUGOGoIBR3GWbzNFTzu0cpy

## Create Push Message in Adobe Mobile Services

You now need to create push message in Adobe Mobile Services
> Please be aware that the audience takes time to update so leave several hours in between you sending your Analytics hit and creating the Push Message

