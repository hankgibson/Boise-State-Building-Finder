package com.hankgibson.boisestatebuildingfinder;
import java.util.Comparator;

/**
 * Created by hankgibson on 11/16/13.
 */
class JSONComparator implements Comparator<Building>
{
    public int compare(Building a, Building b)
    {
        String valA = a.getName();
        String valB = b.getName();
        return valA.compareTo(valB);
    }
}
