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
 * This class manage settings fragment
 */

package com.so2.running;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;


public class ApplicationSettingsFragment extends PreferenceFragment {
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      getActivity().setTitle(getString(R.string.title_settings));

      super.onCreate(savedInstanceState);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.layout.fragment_application_settings);

      //Create a dialog for pref_delete_database
      Preference dialogDelete = (Preference) getPreferenceScreen().findPreference("pref_delete_database");
      dialogDelete.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
         @Override
         public boolean onPreferenceClick(Preference preference) {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

            alertBuilder
                    .setTitle(getString(R.string.deleteDialogTitle))
                    .setMessage(getString(R.string.deleteDataDialogMessage))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener()
                    {
                       @Override
                       public void onClick(DialogInterface dialog, int which)
                       {

                          //Erase all database content
                          RunDbHelper helper = new RunDbHelper(getActivity());
                          SQLiteDatabase db = helper.getWritableDatabase();
                          db.delete("session", null, null);
                          Toast toast = Toast.makeText(getActivity(), getString(R.string.done), Toast.LENGTH_SHORT);
                          toast.show();
                          dialog.dismiss();
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

            return false;
         }
      });

      Preference vibCheckbox = (Preference) getPreferenceScreen().findPreference("pref_vibration");
      vibCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
         public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue.toString().equals("true")) {
               Toast.makeText(getActivity(), "Vibration active", Toast.LENGTH_SHORT).show();
            }
            else
            {
            Toast.makeText(getActivity(), "Vibration not active", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
      });
   }
}