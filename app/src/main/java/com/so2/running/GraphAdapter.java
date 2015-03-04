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
 * This class manage the ViewPager in SessionDeatailFragment
 */

package com.so2.running;


import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class GraphAdapter extends PagerAdapter
{

   //Views, from left to right
   private ArrayList<View> views = new ArrayList<View>();

   //Tell the ViewPager where the page should be displayed, from left-to-right
   // If the page no longer exists, return POSITION_NONE
   @Override
   public int getItemPosition (Object object)
   {
      int index = views.indexOf (object);
      if (index == -1)
         return POSITION_NONE;
      else
         return index;
   }

   // Used by ViewPager, called when ViewPager needs a page to display
   public Object instantiateItem (ViewGroup container, int position)
   {
      View v = views.get (position);
      container.addView (v);
      return v;
   }

   //Called when ViewPager no longer needs a page to display
   @Override
   public void destroyItem (ViewGroup container, int position, Object object)
   {
      container.removeView (views.get (position));
   }

   // Returns the total number of pages that the ViewPage can display
   @Override
   public int getCount ()
   {
      return views.size();
   }

   // Used by ViewPager.
   @Override
   public boolean isViewFromObject (View view, Object object)
   {
      return view == object;
   }

   // Add "view" to right end of "views", returns the position of the new view
   public int addView (View v)
   {
      return addView (v, views.size());
   }

   // Add "view" at "position" to "views", returns position of new view
   public int addView (View v, int position)
   {
      views.add (position, v);
      return position;
   }

   // Removes "view" from "views", retuns position of removed view
   public int removeView (ViewPager pager, View v)
   {
      return removeView (pager, views.indexOf (v));
   }

   // Removes the "view" at "position" from "views", retuns position of removed view
   public int removeView (ViewPager pager, int position)
   {

      pager.setAdapter (null);
      views.remove (position);
      pager.setAdapter (this);

      return position;
   }

   // Returns the "view" at "position"
   public View getView (int position)
   {
      return views.get (position);
   }

}
