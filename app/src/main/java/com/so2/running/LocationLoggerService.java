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
 * This class implements the service used to keep the app functionality in
 * background
 */

package com.so2.running;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.SeekBar;

public class LocationLoggerService extends Service implements LocationListener {

   private final int DATA_UPDATE_INTERVAL = 50; //update interval for graph data collection

   private final IBinder mBinder = new LocalBinder();
   private OnNewGPSPointsListener clientListener;
   private LocationManager locationManager;
   private Location lastLocation = null;
   private boolean started = false;
   private boolean paused = false;
   private long lastUpdate = 0;
   private Chrono mChrono;
   private float distanceFromLastLocation;
   private long updateDifferences;
   private float instantSpeed;
   private float lastSpeed = 3;
   private long lastUpdateMillis = SystemClock.elapsedRealtime();
   private SessionData sessionData = new SessionData();
   private float lastMetersSpeed = 0;
   private int detectionCount = 0;
   private float userSetLastMeters;
   private int reachedTimes = 1; //counter for userSetLastMeter reached
   private int dataUpdateTimes = 0; //counter for dataUpdateInterval reached times
   private int deltaSpeed;
   private int userSetSpeed;
   private SeekBar seekBar = null;
   private long[] lowSpeedPattern = {0, 100, 100, 100, 100, 100, 100};
   private long[] highSpeedPattern = {0, 2000};
   private boolean v;


   @Override
   public void onCreate() {
      subscribeToLocationUpdates();
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
      String prefSpeed = sharedPref.getString("pref_default_last_meters", "50");
      boolean vib = sharedPref.getBoolean("pref_vibration", true);

      userSetLastMeters = Float.valueOf(prefSpeed);
      v = vib;
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      locationManager.removeUpdates(this);
   }

   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   public class LocalBinder extends Binder {
      LocationLoggerService getService(){
         return LocationLoggerService.this;
      }
   }

   //Methods used by client
   public void setStarted(boolean started){
      this.started = started;
      this.paused = !started;
   }

   public boolean getStarted(){
      return started;
   }

   public void setPaused(boolean paused){
      this.paused = paused;
      this.started = !paused;
   }

   public boolean getPaused(){
      return paused;
   }

   public void setChrono(Chrono chrono){
      this.mChrono = chrono;
   }

   public Chrono getChrono(){
      return mChrono;
   }

   public float getInstantSpeed() {
      return instantSpeed;
   }

   public long getLastUpdateMillis(){
      return lastUpdateMillis;
   }

   public SessionData getSessionData(){
      return sessionData;
   }

   public float getLastMetersSpeed(){
      return lastMetersSpeed;
   }

   public void setLastMetersSpeed(float lastMetersSpeed){
      this.lastMetersSpeed = lastMetersSpeed;
   }

   public int getDetectionCount(){
      return detectionCount;
   }

   public void setDetectionCount(int detectionCount){
      this.detectionCount = detectionCount;
   }

   public void setUserSetSpeed(int userSetSpeed){
      this.userSetSpeed = userSetSpeed;
   }

   public void setSeekBar(SeekBar seekBar){
      this.seekBar = seekBar;
   }

   public void addOnNewGPSPointsListener( OnNewGPSPointsListener listener ){
      clientListener = listener;
   }

   public void removeOnNewGPSPointsListener() {
      clientListener = null;
   }

   //When a new location is available notify the client
   public void onLocationChanged(Location newLocation) {

      if(mChrono != null){

         if ((lastLocation == null) || (!started || paused)) {
            lastLocation = newLocation;
            lastUpdate = mChrono.getTimeOnSession();
         } else {
            distanceFromLastLocation = lastLocation.distanceTo(newLocation);
            updateDifferences = mChrono.getTimeOnSession() - lastUpdate;
            instantSpeed = (distanceFromLastLocation * 1000) / (float) updateDifferences;

            lastLocation = newLocation;
            lastUpdate = mChrono.getTimeOnSession();
            lastUpdateMillis = SystemClock.elapsedRealtime();
         }

         if ((instantSpeed < 0.8) && (started && !paused)) {
            instantSpeed = 0;
            lastSpeed = instantSpeed;
         }

         if ((instantSpeed > lastSpeed*2) && (started && !paused) && instantSpeed > 6) {
            instantSpeed = lastSpeed*2;
            lastSpeed = instantSpeed;
         }

         sessionData.setInstantSpeed(instantSpeed);
         sessionData.updateSessionTime(mChrono.getTimeOnSession());

         lastMetersSpeed += instantSpeed;
         detectionCount++;

         if (sessionData.getSessionDistance() > (userSetLastMeters * reachedTimes)) {
            this.sendMessage("updateLastMeterValue");
            reachedTimes++;
         }

         if (sessionData.getSessionDistance() > DATA_UPDATE_INTERVAL * dataUpdateTimes) {
            sessionData.setSessionAltitudes(newLocation.getAltitude());
            sessionData.setSessionSpeeds(instantSpeed);
            sessionData.setSessionTimes(mChrono.getTimeOnSession());
            sessionData.setSessionPaces((Math.pow(instantSpeed * 3.6, -1.0)) * 60);
            dataUpdateTimes++;
         }

         this.sendMessage("updateUI");

         deltaSpeed = (int) ((0 - ((userSetSpeed - instantSpeed) / userSetSpeed)) * RunSessionActivity.DEFAULT_SEEKBAR_MAX_VALUE);
         seekBar.setProgress(deltaSpeed + (RunSessionActivity.DEFAULT_SEEKBAR_MAX_VALUE / 2));

         if (clientListener != null) {
            clientListener.onNewGPSPoint();
         }

         if (v) {
            if (instantSpeed < userSetSpeed) {
               //vibrator.vibrate(lowSpeedPattern, 0);
               System.out.println("lento");
            } else {
               //vibrator.vibrate(highSpeedPattern, 0);
               System.out.println("veloce");
            }
         }
      }
   }

   // Send an Intent with an action named "update-event".
   private void sendMessage(String data) {
      Intent intent = new Intent("update-event");
      // add data
      intent.putExtra("message", data);
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
   }

   @Override
   public void onProviderDisabled(String provider) {
      sendMessage("gpsUnavailable");
   }

   @Override
   public void onProviderEnabled(String provider) {
   }

   @Override
   public void onStatusChanged(String provider, int status, Bundle extras)
   {
      switch (status)
      {
         case LocationProvider.AVAILABLE:
            sendMessage("gpsAvailable");
         case LocationProvider.OUT_OF_SERVICE:
            sendMessage("gpsUnavailable");
         case LocationProvider.TEMPORARILY_UNAVAILABLE:
            sendMessage("gpsUnavailable");
      }
   }

   private void subscribeToLocationUpdates() {
      this.locationManager =
              (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      //Verifica se il GPS e' abilitato altrimenti avvisa l'utente
      if(!locationManager.isProviderEnabled("gps"))
      {
      }
      this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
   }

   //interface for listeners
   public interface OnNewGPSPointsListener {
      public void onNewGPSPoint();
   }

}
