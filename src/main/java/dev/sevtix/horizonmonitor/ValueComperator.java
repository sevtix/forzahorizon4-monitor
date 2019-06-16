package dev.sevtix.horizonmonitor;

import java.util.Comparator;

public class ValueComperator implements Comparator<Value> {

    @Override
    public int compare(Value o1, Value o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}