package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
        }
    }

    public void add(ManaColor color) {
        pool.merge(color, 1, Integer::sum);
    }

    public void clear() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
        }
    }

    public int get(ManaColor color) {
        return pool.getOrDefault(color, 0);
    }

    public int getTotal() {
        int total = 0;
        for (int value : pool.values()) {
            total += value;
        }
        return total;
    }

    public void remove(ManaColor color) {
        pool.merge(color, -1, Integer::sum);
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ManaColor color : ManaColor.values()) {
            map.put(color.getCode(), pool.getOrDefault(color, 0));
        }
        return map;
    }
}
