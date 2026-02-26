package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);
    private int artifactOnlyColorless;

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
        }
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     */
    public ManaPool(ManaPool source) {
        pool.putAll(source.pool);
        this.artifactOnlyColorless = source.artifactOnlyColorless;
    }

    public void add(ManaColor color) {
        pool.merge(color, 1, Integer::sum);
    }

    public void clear() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
        }
        artifactOnlyColorless = 0;
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

    public int getArtifactOnlyColorless() {
        return artifactOnlyColorless;
    }

    public void addArtifactOnlyColorless(int amount) {
        artifactOnlyColorless += amount;
    }

    public void removeArtifactOnlyColorless(int amount) {
        artifactOnlyColorless = Math.max(0, artifactOnlyColorless - amount);
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.getOrDefault(color, 0);
            if (color == ManaColor.COLORLESS) {
                amount += artifactOnlyColorless;
            }
            map.put(color.getCode(), amount);
        }
        return map;
    }
}
