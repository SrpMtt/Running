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
 * This class manage data visualization of the session
 */

package com.so2.running;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionDetail extends Fragment {

   final String X_ARRAY_DISTANCE = "distance";
   final String X_ARRAY_TIMES = "time";


   View view;
   SessionListItem item;
   private ViewPager pager = null;
   private GraphAdapter graphAdapter = null;
   private ArrayList<Button> indicatorArray = new ArrayList<>();

   public void setItem (SessionListItem item)
   {
      this.item = item;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {

      view = inflater.inflate(R.layout.fragment_session_detail, container, false);

      graphAdapter = new GraphAdapter();
      pager = (ViewPager) view.findViewById (R.id.graph_pager);
      pager.setAdapter (graphAdapter);

      //Create all graphs
      GraphView graphTS = (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);
      GraphView graphDS = (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);
      GraphView graphTA = (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);
      GraphView graphDA= (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);
      GraphView graphTP = (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);
      GraphView graphDP = (GraphView) inflater.inflate (R.layout.graph_pager, null).findViewById(R.id.chart);

      //Get textview from view
      TextView date = (TextView) view.findViewById(R.id.session_detail_date);
      TextView distance = (TextView) view.findViewById(R.id.session_detail_distance);
      TextView duration = (TextView) view.findViewById(R.id.session_detail_duration);
      TextView avPace = (TextView) view.findViewById(R.id.session_detail_average_pace);
      TextView avSpeed = (TextView) view.findViewById(R.id.session_detail_average_speed);

      //buttons used as pageview indicator
      Button posIndicator0 = (Button) view.findViewById(R.id.pos0);
      Button posIndicator1 = (Button) view.findViewById(R.id.pos1);
      Button posIndicator2 = (Button) view.findViewById(R.id.pos2);
      Button posIndicator3 = (Button) view.findViewById(R.id.pos3);
      Button posIndicator4 = (Button) view.findViewById(R.id.pos4);
      Button posIndicator5 = (Button) view.findViewById(R.id.pos5);

      //Add indicators to array
      indicatorArray.add(posIndicator0);
      indicatorArray.add(posIndicator1);
      indicatorArray.add(posIndicator2);
      indicatorArray.add(posIndicator3);
      indicatorArray.add(posIndicator4);
      indicatorArray.add(posIndicator5);

      //Set default active indicator
      indicatorCurrent(0);


      //Set data
      date.setText(item.getDate());
      avSpeed.setText(item.getAverageSpeed() + " m/s");
      duration.setText(item.getDuration());
      distance.setText(item.getDistance() + " m");
      avPace.setText(item.getAveragePace() + " min/km");

      //Graph creation
      graphTS = this.initializeGraph(graphTS, item.getJsonTimes(), item.getJsonSpeeds(), getString(R.string.graphTimeSpeed));
      graphDS = this.initializeGraph(graphDS, X_ARRAY_DISTANCE, item.getJsonSpeeds(), getString(R.string.graphDistanceSpeed));
      graphTA = this.initializeGraph(graphTA, item.getJsonTimes(), item.getJsonAltitudes(), getString(R.string.graphTimeAltitude));
      graphDA = this.initializeGraph(graphDA, X_ARRAY_DISTANCE, item.getJsonAltitudes(), getString(R.string.graphDistanceAltitude));
      graphTP = this.initializeGraph(graphTP, item.getJsonTimes(), item.getJsonPaces(), getString(R.string.graphTimePace));
      graphDP = this.initializeGraph(graphDP, X_ARRAY_DISTANCE, item.getJsonPaces(), getString(R.string.graphDistancePace));

      //Add graph to ViewPager
      graphAdapter.addView (graphTS, 0);
      graphAdapter.addView (graphDS, 1);
      graphAdapter.addView (graphTA, 2);
      graphAdapter.addView (graphDA, 3);
      graphAdapter.addView (graphTP, 4);
      graphAdapter.addView (graphDP, 5);
      graphAdapter.notifyDataSetChanged();

      //Set ActionBar title
      getActivity().setTitle(item.getSessionName());

      //Set ViewPager
      pager.setOffscreenPageLimit(6);
      pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

         }

         @Override
         public void onPageSelected(int position) {
            indicatorsNotActive();
            indicatorCurrent(position);
         }

         @Override
         public void onPageScrollStateChanged(int state) {

         }
      });

      return view;
   }

   public void addView (View newPage)
   {
      int pageIndex = graphAdapter.addView (newPage);
      // You might want to make "newPage" the currently displayed page:
      pager.setCurrentItem (pageIndex, true);
   }

   //-----------------------------------------------------------------------------
   // Here's what the app should do to remove a view from the ViewPager.
   public void removeView (View defunctPage)
   {
      int pageIndex = graphAdapter.removeView (pager, defunctPage);
      // You might want to choose what page to display, if the current page was "defunctPage".
      if (pageIndex == graphAdapter.getCount())
         pageIndex--;
      pager.setCurrentItem (pageIndex);
   }

   //-----------------------------------------------------------------------------
   // Here's what the app should do to get the currently displayed page.
   public View getCurrentPage ()
   {
      return graphAdapter.getView (pager.getCurrentItem());
   }

   //-----------------------------------------------------------------------------
   // Here's what the app should do to set the currently displayed page.  "pageToShow" must
   // currently be in the adapter, or this will crash.
   public void setCurrentPage (View pageToShow)
   {
      pager.setCurrentItem (graphAdapter.getItemPosition (pageToShow), true);
   }

   /**
    * Fill a double array from string
    * @param baseString input string
    * @return ArrayList of double
    */
   public ArrayList<Double> getArrayFromJSONArray (String baseString)
   {
      baseString = baseString.replaceAll("[^.?0-9]+", " ");
      ArrayList<Double> baseArray = new ArrayList<Double>();

      List<String> baseStringArray = Arrays.asList(baseString.trim().split(" "));

      for(int i = 0; i < baseStringArray.size(); i++)
      {
         baseArray.add(Double.valueOf(baseStringArray.get(i)));
      }

      return baseArray;
   }

   /**
    * Create graph points
    * @param xValues x-axis values
    * @param yValues y-axis values
    * @return DataPoints array
    */
   public DataPoint[] createPoints (String xValues, String yValues)
   {
      System.out.println("prima if");
      if (xValues.equals("[]") || yValues.equals("[]"))
      {
         System.out.println("inizio if");
         return new DataPoint[] {new DataPoint(0,0)};
      }
      boolean xDistance = false;
      ArrayList<Double> yArray = this.getArrayFromJSONArray(yValues);
      ArrayList<Double> xArray = new ArrayList<Double>();

      System.out.println("dopo if");
      if(xValues.equals(X_ARRAY_DISTANCE))
      {
         xDistance = true;
      }
      else
      {
         //Creiamo l'array dell'asse x (crasha)
         //xArray = this.getArrayFromJSONArray(xValues);

         //non abbiamo fatto in tempo a risolvere il problema
         //prima della consegna, sistemiamo provvisoriamente settando
         //manualmente il valore sull'alle x

      }

      System.out.println("dopo distance if");


      DataPoint[] points = new DataPoint[yArray.size()];
      System.out.println("dop size");

      if (xDistance)
      {
         for(int i = 0; i < yArray.size(); i++)
         {
            points[i] = new DataPoint(50*i, yArray.get(i));
         }
      }
      else
      {
         for(int i = 0; i < yArray.size(); i++)
         {
            //fix provvisorio
            points[i] = new DataPoint(20*i, yArray.get(i));
         }
      }

      return points;
   }

   /**
    * Create the graph
    * @param graph Graph object
    * @param xValue x-axis valus
    * @param yValue y-axis values
    * @param graphTitle graph title
    * @return updated graph
    */
   public GraphView initializeGraph (GraphView graph, String xValue, String yValue, String graphTitle)
   {
      DataPoint[] points = createPoints(xValue, yValue);
      LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint> (points);

      //Graph styling
      graph.addSeries(series);
      graph.getViewport().calcCompleteRange();
      graph.setTitle(graphTitle);
      graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(5);
      graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(5);
      graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
      graph.getGridLabelRenderer().setVerticalLabelsVisible(true);
      graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
      graph.getGridLabelRenderer().reloadStyles();

      return graph;
   }

   /**
    * Set all indicators as not active
    */
   public void indicatorsNotActive ()
   {
      for (Button b : indicatorArray)
      {
         b.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_gray_empty));
      }
   }

   /**
    * Set current indicator as active
    * @param current indicaor position
    */
   public void indicatorCurrent (int current)
   {
      indicatorArray.get(current).setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_blue));
   }

}
