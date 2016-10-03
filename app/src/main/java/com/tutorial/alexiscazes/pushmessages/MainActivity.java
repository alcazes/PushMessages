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
