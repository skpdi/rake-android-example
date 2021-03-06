package com.skp.di.sentinel.rake_android_example;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.rake.android.rkmetrics.RakeAPI;
import com.skp.di.sentinel.rake_android_example.gcm.client.GcmClient;
import com.skp.di.sentinel.rake_android_example.gcm.server.GcmContent;
import com.skp.di.sentinel.rake_android_example.gcm.server.GcmServer;
import com.skplanet.pdp.sentinel.shuttle.AppSampleSentinelShuttle;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "RAKE MAIN ACTIVITY";

    // variables for Rake
    private RakeAPI rake = null;


    // variables for GCM
    private Context context = null;
    private GcmClient gcmClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        // initialize Rake instance
        RakeAPI.setDebug(true); // print debug messages. Default: false

        /*
            arg1: this context
            arg2: rake token which are generated by Sentinel
            arg3: if this argument is true, rake will flush log into dev server,
                  otherwise log will be sent to live server.
                  Also if this argument is true, every log will be sent to dev server instantly
                  without saving into SQLite
         */
        rake = RakeAPI.getInstance(this, RakeConfig.TOKEN, RakeConfig.IS_DEV_MODE);

        // button to track a log.
        Button btnTrack = (Button) findViewById(R.id.btnTrack);
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialize shuttle
                AppSampleSentinelShuttle shuttle = new AppSampleSentinelShuttle();

                // shuttle provides a logging method per action.
                // let action name is 'action4'
                shuttle.setBodyOfaction4("field1 value", "field3 value", "field4 value");

                // track a log. log will be saved into SQLite (local storage)
                Log.d("shuttle string", shuttle.toJSONString());
                rake.track(shuttle.toJSONObject());

                // if you need to send a log immediately, flush a log after tracking
                // rake.flush();
            }
        });

        // button to flush log.
        Button btnFlush = (Button) findViewById(R.id.btnFlush);
        btnFlush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rake.flush();
            }
        });

        // buttons for GCM
        Button btnGCMRegisterDevice = (Button) findViewById(R.id.btnGCMRegisterDevice);
        Button btnGCMPushMessage = (Button) findViewById(R.id.btnGCMPushMessage);

        gcmClient = new GcmClient(this, context);

        btnGCMRegisterDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcmClient.registerDevice();
            }
        });

        btnGCMPushMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcmContent content = new GcmContent(gcmClient.getRegistrationID());
                content.createData("title", "Hello, GCM!");
                content.createData("message", "This is first GCM push message");
                GcmServer.postMessageToGCM(content);
            }
        });

    }

    @Override
    protected void onStop() {
        // you can flush tracked logs in onStop() according to your flushing policy
        rake.flush();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // you must flush tracked logs in onDestroy().
        rake.flush();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
