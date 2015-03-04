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
 * This class manage the android SQLite database
 */

package com.so2.running;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunDbHelper extends SQLiteOpenHelper
{
   private static final String DB_NOME = "Training_Sessions_Db";
   private static final int DB_VERSIONE = 1;
   private static final String TABLE_NAME = "session";
   private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RunDbHelper.TABLE_NAME;

   public RunDbHelper(Context context)
   {
      super(context, DB_NOME, null, DB_VERSIONE);
   }

   @Override
   //Create database table
   public void onCreate(SQLiteDatabase db)
   {
      String sql = "CREATE TABLE " + TABLE_NAME;
      sql += "(_id INTEGER PRIMARY KEY,"; //0
      sql += "sessionName TEXT NOT NULL,"; //1
      sql += "date TEXT,"; //2
      sql += "distance TEXT,"; //3
      sql += "averageSpeed TEXT,"; //4
      sql += "averagePace TEXT,"; //5
      sql += "speedLastMeters TEXT,"; //6
      sql += "paceLastMeters TEXT,"; //7
      sql += "speeds TEXT,"; //8
      sql += "altitudes TEXT,"; //9
      sql += "duration TEXT,"; //10
      sql += "times TEXT,"; //11
      sql += "paces TEXT);"; //12

      db.execSQL(sql);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
   {
      db.execSQL(SQL_DELETE_ENTRIES);
      onCreate(db);
   }

   /**
    * Write data into database
    * @param data data collected during session
    * @param sessionName name of the session
    * @param mChrono Chrono instance in session */
   public void writeIntoDb(SessionData data, String sessionName, Chrono mChrono) throws IOException, JSONException
   {
      Calendar trainingDay = Calendar.getInstance();
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy  HH:mm");
      String formattedDate = dateFormat.format(trainingDay.getTime());

      ContentValues obj = new ContentValues();
      obj.put("_id", findNextID());
      obj.put("sessionName", sessionName);
      obj.put("date", formattedDate);
      obj.put("duration", mChrono.getSessionLenght());
      obj.put("distance", String.format("%.02f", data.getSessionDistance()));
      obj.put("averageSpeed", String.format("%.02f", data.getAverageSpeed()));
      obj.put("speedLastMeters", String.format("%.02f", data.getAverageSpeedLastXMeter()));
      obj.put("averagePace", String.format("%.02f", data.getAveragePace()));
      obj.put("paceLastMeters", String.format("%.02f", data.getAveragePaceLastXMeter()));
      obj.put("speeds", data.getSessionSpeeds().toString());
      obj.put("altitudes", data.getSessionAltitudes().toString());
      obj.put("times", data.getSessionTimes().toString());
      obj.put("paces", data.getSessionPaces().toString());

      //Write into database
      SQLiteDatabase db = this.getWritableDatabase();
      db.insert("session", null, obj);
   }

   /**
    * Find next usable ID (highest id + 1)
    * @return nextID*/
   int findNextID ()
   {
      SQLiteDatabase dbRead = this.getReadableDatabase();
      int nextID;
      try
      {
         Cursor c = dbRead.rawQuery("SELECT _id FROM session WHERE _id =( SELECT max(_id) FROM session)", null);
         c.moveToFirst();
         nextID = c.getInt(0)+1;
      }
      catch (Exception e)
      {
         nextID = 1;
      }

      return nextID;
   }


   /**
    * Export all sessions, create a JSON file for each session
    * @param exportLocationPath path on external storage
    */
   public void exportDb(String exportLocationPath)
   {

      //Access database
      SQLiteDatabase db = this.getReadableDatabase();
      String selectQuery = "SELECT * FROM session";
      Cursor cursor = db.rawQuery(selectQuery, null);
      SessionListItem item;

      if (cursor.moveToFirst()){

         //Get data
         do{
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
            item.setJsonTimes(cursor.getString(11));
            item.setJsonPaces(cursor.getString(12));


            //Create file
            try
            {
               String fileName = item.getSessionName() + "_" + item.getId();
               FileWriter file = new FileWriter(exportLocationPath + fileName + ".json");
               file.write(item.toJSONObject().toString());
               file.flush();
               file.close();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         } while (cursor.moveToNext());
      }
   }

   /**
    * Import session from JSON
    * @param dbToImport JSON file containing session data
    * */
   public boolean importTraining(File dbToImport)
   {
      StringBuilder text = new StringBuilder();
      boolean result = false;

      try {
         BufferedReader br = new BufferedReader(new FileReader(dbToImport));
         String line;

         while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
         }

         JSONObject session = new JSONObject(text.toString());
         br.close();
         ContentValues obj = new ContentValues();
         obj.put("_id", findNextID());
         try {
            obj.put("sessionName", session.getString("sessionName"));
            obj.put("date", session.getString("date"));
            obj.put("duration", session.getString("duration"));
            obj.put("distance", session.getString("distance"));
            obj.put("averageSpeed", session.getString("averageSpeed"));
            obj.put("averagePace", session.getString("averagePace"));
            obj.put("speeds", session.getString("speeds"));
            obj.put("altitudes", session.getString("altitudes"));
            obj.put("times", session.getString("times"));
            obj.put("paces", session.getString("paces"));
            obj.put("speedLastMeters", "0");
            obj.put("paceLastMeters", "0");


            //Accedo al database in scrittura
            SQLiteDatabase db = this.getWritableDatabase();
            db.insert("session", null, obj);
            result = true;
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return result;
   }

   /**
    * Export session from JSON
    * @param trainingID session id
    * @param exportLocationPath path on external storage
    * */
   public boolean exportTraining(int trainingID, String exportLocationPath)
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

      //Accedo al database in sola lettura
      SQLiteDatabase db = this.getReadableDatabase();

      String[] selectionArgs = {trainingID + ""};

      String selectQuery = "SELECT * FROM session where _id = ?";
      Cursor cursor = db.rawQuery(selectQuery, selectionArgs);
      SessionListItem item;

      if (cursor.moveToFirst()){

         //acquisizione dati
         do{
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
            item.setJsonTimes(cursor.getString(11));
            item.setJsonPaces(cursor.getString(12));

         } while (cursor.moveToNext());

         //Save file
         try {
            String fileName = item.getSessionName() + "_" + item.getId();
            FileWriter file = new FileWriter(exportLocationPath + fileName + ".json");
            file.write(item.toJSONObject().toString());
            file.flush();
            file.close();

            return true;
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return false;
   }
}
