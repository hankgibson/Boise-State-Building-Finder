package com.hankgibson.boisestatebuildingfinder.adapter;

/**
 * Created by hankgibson on 11/13/13.
 */
import com.hankgibson.boisestatebuildingfinder.Building;
import com.hankgibson.boisestatebuildingfinder.BuildingController;
import com.hankgibson.boisestatebuildingfinder.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class NavDrawerListAdapter extends BaseAdapter
{
    private Filter filter;
    private Context context;
    private ArrayList<Building> navDrawerItems;
    private ArrayList<Building> filteredResults;


    public NavDrawerListAdapter(Context context, ArrayList<Building> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
        this.filteredResults = navDrawerItems;
    }

    @Override
    public int getCount() {
        return filteredResults.size();
    }

    public ArrayList<Building> getList()
    {
        return filteredResults;
    }

    @Override
    public Building getItem(int position) {
        return filteredResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        // configure the view
        Building currentBuilding = getItem(position);

        TextView nameTextView = (TextView)convertView.findViewById(R.id.building_name);
        nameTextView.setText( currentBuilding.getName() );

        return convertView;
    }

    public Filter getFilter(){

        if(filter == null){
            filter = new BuildingFilter();
        }
        return filter;
    }


    private class BuildingFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0)
            {
                results.values = BuildingController.getBuildings();
                results.count = BuildingController.getBuildings().size();
            }

            else
            {
                ArrayList<Building> newValues = new ArrayList<Building>();
                for(int i = 0; i < navDrawerItems.size(); i++)
                {
                    Building item = navDrawerItems.get(i);
                    if(item.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                    {
                        newValues.add(item);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();

            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (results.count > 0)
            {
                filteredResults = (ArrayList<Building>)results.values;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}