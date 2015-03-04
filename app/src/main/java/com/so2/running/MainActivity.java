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
 * This is the application main activity
 */

package com.so2.running;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends ActionBarActivity
{
   final Context context = this;
   private DrawerLayout mDrawerLayout;
   private ListView mDrawerList;
   private String[] itemList; //Navigation drawer items
   private ActionBarDrawerToggle mDrawerToggle;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      //Set ActionBar
      ActionBar actionBar = getSupportActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setElevation(0);
      actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.SeaGreen)));

      //Set NavigationDrawer
      mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      mDrawerList = (ListView) findViewById(R.id.left_drawer);
      mDrawerList.setBackgroundResource(R.color.SeaGreen);
      itemList = getResources().getStringArray(R.array.item_list);

      //Set the content of the activity with the MainFagment
      MainActivity.changeFragment(getFragmentManager(), new MainFragment());

      // Set the adapter for the list view
      mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, itemList));
      mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener()
      {
         //Manage events on NavigationDrawer items click
         public void onItemClick(AdapterView parent, View view, int position, long id)
         {

            switch (position)
            {
               //Go to session list
               case 0:
                  FragmentManager sessionsFragmentManager = getFragmentManager();
                  sessionsFragmentManager.popBackStackImmediate(null, sessionsFragmentManager.POP_BACK_STACK_INCLUSIVE);
                  sessionsFragmentManager.beginTransaction()
                          .replace(R.id.content_frame, new SessionListFragment())
                          .commit();

                  mDrawerLayout.closeDrawer(mDrawerList);
                  break;

               //Show import dialog
               case 1:
                  AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                  alertBuilder
                          .setTitle(getString(R.string.importDialogTitle))
                          .setMessage(getString(R.string.importDialogMessage))
                          .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                             }
                          });

                  AlertDialog endSessionDialog = alertBuilder.create();

                  endSessionDialog.show();
                  mDrawerLayout.closeDrawer(mDrawerList);
                  break;

               //Show export dialog
               case 2:
                  MainActivity.alertExportDialogBehavior(context);
                  mDrawerLayout.closeDrawer(mDrawerList);
                  break;

               //Go to settings
               case 3:
                  MainActivity.changeFragment(getFragmentManager(), new ApplicationSettingsFragment());
                  mDrawerLayout.closeDrawer(mDrawerList);
                  break;

               //Go to info
               case 4:
                  MainActivity.changeFragment(getFragmentManager(), new InfoFragment());
                  mDrawerLayout.closeDrawer(mDrawerList);
                  break;
            }

         }
      });


      mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
      {
         @Override
         public void onDrawerOpened(View drawerView)
         {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu();
         }

         @Override
         public void onDrawerClosed(View drawerView)
         {
            super.onDrawerClosed(drawerView);
            invalidateOptionsMenu();
         }
      };

      mDrawerLayout.setDrawerListener(mDrawerToggle);
   }

   // Called when invalidateOptionsMenu() is invoked
   public boolean onPrepareOptionsMenu(Menu menu) {
      return super.onPrepareOptionsMenu(menu);
   }

   // Pass the event to ActionBarDrawerToggle
   public boolean onOptionsItemSelected(MenuItem item) {
      if (mDrawerToggle.onOptionsItemSelected(item)) {
         return true;
      }
      return false;
   }

   //If a JSON file is opened, the application import the session
   protected void onStart() {
      super.onStart();

      final Intent intent = getIntent();
      if (intent != null){

         //got intent
         final android.net.Uri data = intent.getData ();

         if (data != null){

            //got data
            RunDbHelper dbHelper = new RunDbHelper(getApplicationContext());
            if(dbHelper.importTraining(new File(data.getEncodedPath()))){
               Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
            }
            else{
               Toast.makeText(this, R.string.importFailed, Toast.LENGTH_SHORT).show();
            }

         }
      }
   }

   //Update session list on restart
   protected void onRestart ()
   {
      super.onRestart();
      FragmentManager sessionsFragmentManager = getFragmentManager();
      sessionsFragmentManager.popBackStackImmediate(null, sessionsFragmentManager.POP_BACK_STACK_INCLUSIVE);
      sessionsFragmentManager.beginTransaction()
              .replace(R.id.content_frame, new SessionListFragment())
              .commit();
   }

   @Override
   protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      mDrawerToggle.syncState();
   }

   /**
    * Change the activity content with the new fragment
    * @param manager FrgamentManager
    * @param newFragment new fragment to display
    */
   public static void changeFragment (FragmentManager manager, Fragment newFragment)
   {
      FragmentManager fragmentManager = manager;
      fragmentManager.beginTransaction()
              .replace(R.id.content_frame, newFragment)
              .addToBackStack("")
              .commit();
   }

   @Override
   //Back button override
   public void onBackPressed()
   {
      FragmentManager fm = getFragmentManager();

      if(fm.getBackStackEntryCount()>0)
      {
         fm.popBackStackImmediate(null, fm.POP_BACK_STACK_INCLUSIVE);

         fm.beginTransaction()
                 .replace(R.id.content_frame, new SessionListFragment())
                 .commit();
      }
      else
      {
         super.onBackPressed();
      }
   }

   /**
    * Create export dialog
    * @param context activity context
    */
   public static void alertExportDialogBehavior (final Context context)
   {
      String storageDir = Environment.getExternalStorageDirectory() + "/Running/";
      final File saveFolder = new File(storageDir);
      boolean success = true;

      //Verify memory availability
      if (!saveFolder.exists())
      {
         success = saveFolder.mkdir();
      }

      if (!success)
      {
         //TODO: handle error
      }

      //Set export path
      final String path = Environment.getExternalStorageDirectory()+ "/Running/";


      //Create the dialog
      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
      alertBuilder
              .setTitle("Export sessions")
              .setMessage("Do you want to export sessions? \nFiles will be exported in: " + path)
              .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {

                    RunDbHelper helper = new RunDbHelper(context);
                    helper.exportDb(path);

                    //CharSequence text = Resources.getSystem().getString(R.string.done);
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, R.string.done, duration);
                    toast.show();

                    dialog.dismiss();
                 }
              })
              .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                 }
              });

      AlertDialog endSessionDialog = alertBuilder.create();

      endSessionDialog.show();
   }
}

