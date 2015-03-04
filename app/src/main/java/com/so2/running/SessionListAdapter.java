package com.so2.running;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Studente on 24/02/2015.
 */
public class SessionListAdapter extends ArrayAdapter<SessionListItem>
{
   private LayoutInflater mInflater;
   private ArrayList<SessionListItem> sessionList;

   public SessionListAdapter(Context context, int resource, List<SessionListItem> objects) {
      super(context, resource, objects);
      sessionList = (ArrayList) objects;
      mInflater = LayoutInflater.from(context);
   }

   public int getCount() {
      return sessionList.size();
   }

   @Override
   public SessionListItem getItem(int arg0) {
      return sessionList.get(arg0);
   }

   @Override
   public long getItemId(int arg0) {
      return arg0;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      // TODO Auto-generated method stub
      TextView name;
      TextView date;
      TextView distance;

         convertView = mInflater.inflate(R.layout.session_listview_item, null);
         name = (TextView) convertView.findViewById(R.id.session_name_item);
         date = (TextView) convertView.findViewById(R.id.session_date_item);
         distance = (TextView) convertView.findViewById(R.id.session_distance_item);



      name.setText(sessionList.get(position).getSessionName());
      date.setText(sessionList.get(position).getDate());
      distance.setText(sessionList.get(position).getDistance() + " m");


      return convertView;
   }


}
