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
 * This fragment contains the home page
 */

package com.so2.running;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class MainFragment extends Fragment {

   private Button newSessionButton;
   private Button sessionListButton;
   private boolean isGPSFix = false;
   private long lastUpdateMillis;   //used to verify GPS availability
   private Location lastLocation;   //used to verify GPS availability
   private View view;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {

      view = inflater.inflate(R.layout.fragment_main_content, container, false);

      newSessionButton = (Button) view.findViewById(R.id.newSessionButton);
      newSessionButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v) {
            //verify GPS availability
            if (isGPSFix)
            {
               DialogFragment newFragment = new NewSessionDialog();
               newFragment.show(getFragmentManager(), "New Training");
            }
            else
            {
               Toast.makeText(getActivity(), R.string.gpsNoFix, Toast.LENGTH_SHORT).show();
            }
         }
      });

      //Go to session list
      sessionListButton = (Button) view.findViewById(R.id.sessionListButton);
      sessionListButton.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            FragmentManager sessionsFragmentManager = getFragmentManager();
            sessionsFragmentManager.popBackStackImmediate(null, sessionsFragmentManager.POP_BACK_STACK_INCLUSIVE);
            sessionsFragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new SessionListFragment())
                    .commit();
         }
      });

      //verify GPS availability
      final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
      GpsStatus.Listener gpsListener = new GpsStatus.Listener()
      {
         @Override
         public void onGpsStatusChanged(int event)
         {
            switch (event)
            {
               case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                  if (lastLocation != null)
                  {
                     isGPSFix = (SystemClock.elapsedRealtime() - lastUpdateMillis < 3000);
                  }

                  if (isGPSFix) {
                     // A fix has been acquired.
                  }
                  else
                  {
                     // The fix has been lost.
                  }
                  break;

               case GpsStatus.GPS_EVENT_FIRST_FIX:
                  // Do something.
                  isGPSFix = true;
                  break;
            }
         }
      };

      final LocationListener locationListener = new LocationListener()
      {
         public void onLocationChanged(Location newLocation)
         {
            lastUpdateMillis = SystemClock.elapsedRealtime();
            lastLocation = newLocation;
         }

         public void onStatusChanged(String provider, int status, Bundle extras) {
         }

         public void onProviderEnabled(String provider) {
         }

         public void onProviderDisabled(String provider) {
         }
      };

      locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
      locationManager.addGpsStatusListener(gpsListener);

      return view;
   }
}
