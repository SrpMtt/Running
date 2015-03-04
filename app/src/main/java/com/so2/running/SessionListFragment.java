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
 * This fragment define the session list content
 */

package com.so2.running;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;


public class SessionListFragment extends Fragment
{

   private boolean isGPSFix = false;
   private long lastUpdateMillis;
   private Location lastLocation;

   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState)
   {
      getActivity().setTitle(getString(R.string.title_session_list));

      View view = inflater.inflate(R.layout.fragment_session_list, container, false);

      final ListView lv = (ListView) view.findViewById(R.id.session_list_view);
      ArrayList<SessionListItem> sessionList = getSessionList();

      //If there are no sessions emtyListFragment is called
      if (sessionList.size() == 0)
      {
         FragmentManager fm = getFragmentManager();
         fm.beginTransaction()
                 .replace(R.id.content_frame, new EmptyListFragment())
                 .commit();
      }

      //Visualize session list
      else
      {
         lv.setAdapter(new SessionListAdapter(getActivity(), R.layout.session_listview_item, sessionList));

         lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
         {
            //Go to session detail
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

               // selected item
               final SessionListItem item = (SessionListItem) lv.getItemAtPosition(position);

               SessionDetail itemDetail = new SessionDetail();
               itemDetail.setItem(item);

               MainActivity.changeFragment(getFragmentManager(), itemDetail);
            }
         });

         lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
         {
            //Show single item options
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
               final AlertDialog.Builder itemOptionDialog = new AlertDialog.Builder(getActivity());
               final SessionListItem item = (SessionListItem) lv.getItemAtPosition(position);
               itemOptionDialog.setTitle(R.string.itemOptionsDialogTitle)
                       .setItems(R.array.item_options, new DialogInterface.OnClickListener() {

                          public void onClick(DialogInterface dialog, int which) {
                             RunDbHelper helper = new RunDbHelper(getActivity());

                             switch (which)
                             {
                                case 0:
                                   //Export session
                                   String exportPath = Environment.getExternalStorageDirectory() + "/Running/";
                                   helper.exportTraining(item.getId(), exportPath);
                                   Toast.makeText(getActivity(), R.string.exported, Toast.LENGTH_SHORT).show();
                                   break;

                                case 1:
                                   //Delete session
                                   SQLiteDatabase db = helper.getWritableDatabase();
                                   db.delete("session", "_id = " + item.getId(), null);
                                   Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_SHORT).show();
                                   MainActivity.changeFragment(getFragmentManager(), new SessionListFragment());
                                   break;
                             }
                          }
                       });
               itemOptionDialog.show();

               return true;
            }
         });


         Button newSessionButton = (Button) view.findViewById(R.id.newSessionButton2);

         newSessionButton.setOnClickListener(new View.OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               //Verify GPS availability
               if (isGPSFix)
               {
                  DialogFragment newFragment = new NewSessionDialog();
                  newFragment.show(getFragmentManager(), "New Training");
               } else {
                  Toast.makeText(getActivity(), R.string.gpsNoFix, Toast.LENGTH_SHORT).show();
               }
            }
         });

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

      return view;
   }

   /**
    * Collects all sessions from database
    * @return session list
    */
   public ArrayList<SessionListItem> getSessionList ()
   {
      ArrayList<SessionListItem> sessionList = new ArrayList<SessionListItem>();

      String selectQuery = "SELECT  * FROM session";
      RunDbHelper helper = new RunDbHelper(getActivity());
      SQLiteDatabase db = helper.getWritableDatabase();
      Cursor cursor = db.rawQuery(selectQuery, null);

      SessionListItem item;

      if (cursor.moveToFirst())
      {
         do
         {
            item = new SessionListItem();
            item.setId(cursor.getInt(0));
            item.setSessionName(cursor.getString(1));
            item.setDate(cursor.getString(2));
            item.setDistance(cursor.getString(3));
            item.setAverageSpeed(cursor.getString(4));
            item.setAveragePace(cursor.getString(5));
            item.setDuration(cursor.getString(10));
            item.setJsonSpeeds(cursor.getString(8));
            item.setJsonAltitudes(cursor.getString(9));
            item.setJsonPaces(cursor.getString(12));
            item.setJsonTimes(cursor.getString(11));

            sessionList.add(item);
         } while (cursor.moveToNext());
      }

     return sessionList;
   }


}


