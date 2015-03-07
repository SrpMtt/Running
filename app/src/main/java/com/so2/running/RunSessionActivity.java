/*
                    RUNNING
   Copyright (C) 2015  Alessandro Mereu, Maurizio Romano, Matteo Enrico Serpi

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class implements all the functionality of the training session
 */

package com.so2.running;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import org.json.JSONException;
import java.io.IOException;

public class RunSessionActivity extends Activity {

   public final static int DEFAULT_SEEKBAR_MAX_VALUE = 100;

   // The primary interface we will be calling on the service
   private LocationLoggerService mService = null;
   private boolean mIsBound;
   private Intent serviceIntent;
   final Context context = this;
   private Chrono mChrono;
   private boolean started = false;
   private boolean paused = false;
   private double lastMetersPace = 0;
   private String sessionName;


   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_run_session);

      //Initialize intent
      serviceIntent = new Intent(this, LocationLoggerService.class);

      mChrono = new Chrono((Chronometer) findViewById(R.id.chronometer1));

      final Button startButton = (Button) findViewById(R.id.startButton);
      final Button stopButton = (Button) findViewById(R.id.stopButton);
      final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);  //used for speed indicator

      stopButton.setEnabled(false);
      stopButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_gray));

      seekBar.setMax(DEFAULT_SEEKBAR_MAX_VALUE);
      seekBar.setProgress(DEFAULT_SEEKBAR_MAX_VALUE / 2);

      Bundle b = getIntent().getExtras();
      sessionName = b.getString("sessionName");

      //For freeze the seekbar without disable it
      seekBar.setOnTouchListener(new View.OnTouchListener()
      {
         @Override
         public boolean onTouch(View v, MotionEvent event)
         {
            return true;
         }
      });
      seekBar.setBackgroundColor(Color.TRANSPARENT);

      //Manage button color and functionality
      startButton.setOnClickListener(new View.OnClickListener()
      {
         public void onClick (View view)
         {
            if(paused) {
               mChrono.resumeChronometer();
               mService.setStarted(true);
               started = true;
               paused = false;
               startButton.setText(getString(R.string.pauseButton));
               startButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_blue));
            }
            else if (!started)
            {
               mChrono.startChronometer();
               mService.setStarted(true);
               started = true;
               stopButton.setEnabled(true);
               stopButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_red));
               startButton.setText(getString(R.string.pauseButton));
               startButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_blue));
            }
            else
            {
               mChrono.pauseChronometer();
               mService.setPaused(true);
               started = false;
               paused = true;
               startButton.setText(getString(R.string.resumeButton));
               startButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_green));
            }
         }
      });

      stopButton.setOnClickListener(new View.OnClickListener()
      {
         public void onClick(View view)
         {
            stopBehavior();
         }
      });
   }


   //Class for interacting with the main interface of the service.
   private ServiceConnection mConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName className, IBinder
              service) {
         // This is called when the connection with the service has been
         // established, giving us the service object we can use to
         // interact with the service.
         LocationLoggerService.LocalBinder binder = (LocationLoggerService.LocalBinder) service;
         mService = binder.getService();
         mIsBound = true;

         LocationLoggerService.OnNewGPSPointsListener clientListener = new
                 LocationLoggerService.OnNewGPSPointsListener() {
                    @Override
                    public void onNewGPSPoint() {
                       getGPSData();
                    }
                 };
         //Register listener
         mService.addOnNewGPSPointsListener(clientListener);

         mService.setChrono(mChrono);
         mService.setSeekBar((SeekBar) findViewById(R.id.seekBar));
         mService.setUserSetSpeed(getIntent().getExtras().getInt("userSetSpeed"));
      }

      @Override
      public void onServiceDisconnected(ComponentName className) {
         // This is called when the connection with the service has been
         // unexpectedly disconnected -- that is, its process crashed.
         mIsBound = false;
         //TODO qui andra' gestita la situazione...
      }
   };

   private void connectLocalService() {
      bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
   }

   private void disconnectLocalService() {
      if(mIsBound) {
         // Detach our existing connection.
         unbindService(mConnection);
         mIsBound = false;
      }
   }

   public void getGPSData(){
      //do something
   }

   @Override
   protected void onStart() {
      super.onStart();
      //Connect to service
      connectLocalService();
   }

   @Override
   protected void onStop() {
      super.onStop();
      //Unregister listener
      mService.removeOnNewGPSPointsListener();
      //unbind
      disconnectLocalService();
   }

   public void onResume() {
      super.onResume();

      // Register mMessageReceiver to receive messages.
      LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
              new IntentFilter("update-event"));
   }

   // handler for received Intents for the "my-event" event
   private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         // Extract data included in the Intent
         String message = intent.getStringExtra("message");
         //got message do something
         if(message.equals("updateLastMeterValue")){
            updateLastMeterValue();
         }
         else if (message.equals("gpsAvailable"))
         {
            ((TextView) findViewById(R.id.gpsStatus)).setText(getString(R.string.gpsFix));
         }
         else if (message.equals("gpsUnavailable"))
         {
            ((TextView) findViewById(R.id.gpsStatus)).setText(getString(R.string.gpsNoFix));
         }
         else if(message.equals("updateUI")){
            updateUI();
         }
      }
   };

   @Override
   protected void onPause() {
      // Unregister since the activity is not visible
      LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
      super.onPause();
   }

   public boolean isExternalStorageWritable()
   {
      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state)) {
         return true;
      }
      return false;
   }

   public void updateLastMeterValue ()
   {
      mService.setLastMetersSpeed(mService.getLastMetersSpeed() / mService.getDetectionCount());
      lastMetersPace = Math.pow(mService.getLastMetersSpeed() * 3.6, -1.0) * 60;
      mService.getSessionData().setAverageSpeedLastXMeter(mService.getLastMetersSpeed());
      mService.getSessionData().setAveragePaceLastXMeter(lastMetersPace);

      ((TextView) findViewById(R.id.averageLMspeedValue)).setText(String.format("%.02f", mService.getLastMetersSpeed()) + " m/s");
      ((TextView) findViewById(R.id.averageLMPaceValue)).setText(String.format("%.02f", lastMetersPace) + " min/km");

      mService.setLastMetersSpeed(0);
      mService.setDetectionCount(0);

   }

   public void updateUI ()
   {
      ((TextView) findViewById(R.id.speedView)).setText(String.format("%.02f", mService.getInstantSpeed()) + " m/s");
      ((TextView) findViewById(R.id.averageSpeedValue)).setText(String.format("%.02f", mService.getSessionData().getAverageSpeed()) + " m/s");
      ((TextView) findViewById(R.id.sessionDistanceView)).setText(String.format("%.02f", mService.getSessionData().getSessionDistance()) + " m");
      ((TextView) findViewById(R.id.averagePaceValue)).setText(String.format("%.02f", mService.getSessionData().getAveragePace()) + " min/km");

   }


   @Override
   //Back button override
   public void onBackPressed()
   {
      if(started)
      {
         this.stopBehavior();
      }
      else
      {
         super.onBackPressed();
      }
   }

   /**
    * Define stop button behavior
    */
   public void stopBehavior()
   {
      mChrono.pauseChronometer();
      Button startButton = (Button) findViewById(R.id.startButton);
      startButton.setText(getString(R.string.resumeButton));
      startButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_green));
      started = false;
      paused = true;
      mService.setPaused(true);

      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
      alertBuilder
              .setTitle(getString(R.string.endSessionDialogTitle))
              .setMessage(getString(R.string.endSessionDialogMessage))
              .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener()
              {
                 @Override
                 public void onClick(DialogInterface dialog, int which)
                 {

                    if (isExternalStorageWritable())
                    {
                       try
                       {
                          RunDbHelper dbHelper = new RunDbHelper(getApplicationContext());
                          dbHelper.writeIntoDb(mService.getSessionData(), sessionName, mChrono);
                       }
                       catch (IOException exIO)
                       {
                          //TODO: gestire l'errore
                       }
                       catch (JSONException exJS)
                       {
                          //TODO: gestire l'errore
                       }
                    }
                    else
                    {
                       //TODO: gestire l'errore
                    }
                    finish();
                 }
              })
              .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener()
              {
                 @Override
                 public void onClick(DialogInterface dialog, int which)
                 {
                    dialog.dismiss();
                 }
              });

      AlertDialog endSessionDialog = alertBuilder.create();

      endSessionDialog.show();
   }

}