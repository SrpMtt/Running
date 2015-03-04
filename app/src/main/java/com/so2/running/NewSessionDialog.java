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
 * This class create the dialog that starts a new session
 */

package com.so2.running;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.DialogFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


public class NewSessionDialog extends DialogFragment
{

   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      // Get the layout inflater
      LayoutInflater inflater = getActivity().getLayoutInflater();
      View view = inflater.inflate(R.layout.fragment_new_session_dialog, null);


      final EditText inputSessionName = (EditText) view.findViewById(R.id.sessionName);
      final NumberPicker inputUserSpeed = (NumberPicker) view.findViewById(R.id.speedPicker);

      //Set picker
      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
      String prefSpeed = sharedPref.getString("pref_default_speed", "3");
      inputUserSpeed.setMaxValue(50);
      inputUserSpeed.setMinValue(1);
      inputUserSpeed.setValue(Integer.valueOf(prefSpeed));

      RunDbHelper helper = new RunDbHelper(getActivity());

      //Set default session name
      String formattedName = "Allenamento_" + helper.findNextID();
      inputSessionName.setText(formattedName);


      // Inflate and set the layout for the dialog
      // Pass null as the parent view because its going in the dialog layout
      builder.setView(view)
              .setTitle(getString(R.string.newSessionDialogTitle))
              // Add action buttons
              .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(getActivity(), RunSessionActivity.class);
                    Bundle b = new Bundle();

                    b.putString("sessionName", inputSessionName.getText().toString());
                    b.putInt("userSetSpeed", inputUserSpeed.getValue());
                    i.putExtras(b);

                    startActivity(i);
                 }
              })
              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    dismiss();
                 }
              });

      return builder.create();
   }
}
