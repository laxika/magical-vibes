package com.github.laxika.magicalvibes.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ManaPool {

    private int white;
    private int blue;
    private int black;
    private int red;
    private int green;

    public void add(String color) {
        switch (color) {
            case "W" -> white++;
            case "U" -> blue++;
            case "B" -> black++;
            case "R" -> red++;
            case "G" -> green++;
        }
    }

    public void clear() {
        white = 0;
        blue = 0;
        black = 0;
        red = 0;
        green = 0;
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("W", white);
        map.put("U", blue);
        map.put("B", black);
        map.put("R", red);
        map.put("G", green);
        return map;
    }
}
