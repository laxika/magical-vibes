package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> creatureMana = new EnumMap<>(ManaColor.class);
    private int artifactOnlyColorless;
    private int myrOnlyColorless;
    private int restrictedRed;

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            creatureMana.put(color, 0);
        }
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     */
    public ManaPool(ManaPool source) {
        pool.putAll(source.pool);
        creatureMana.putAll(source.creatureMana);
        this.artifactOnlyColorless = source.artifactOnlyColorless;
        this.myrOnlyColorless = source.myrOnlyColorless;
        this.restrictedRed = source.restrictedRed;
    }

    public void add(ManaColor color) {
        pool.merge(color, 1, Integer::sum);
    }

    public void add(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
    }

    public void clear() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            creatureMana.put(color, 0);
        }
        artifactOnlyColorless = 0;
        myrOnlyColorless = 0;
        restrictedRed = 0;
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
        // Clamp creature mana so it never exceeds total for this color
        int total = pool.getOrDefault(color, 0);
        int creature = creatureMana.getOrDefault(color, 0);
        if (creature > total) {
            creatureMana.put(color, total);
        }
    }

    public void addCreatureMana(ManaColor color, int amount) {
        creatureMana.merge(color, amount, Integer::sum);
    }

    public int getCreatureMana(ManaColor color) {
        return creatureMana.getOrDefault(color, 0);
    }

    public int getCreatureManaTotal() {
        int total = 0;
        for (int value : creatureMana.values()) {
            total += value;
        }
        return total;
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

    public int getMyrOnlyColorless() {
        return myrOnlyColorless;
    }

    public void addMyrOnlyColorless(int amount) {
        myrOnlyColorless += amount;
    }

    public void removeMyrOnlyColorless(int amount) {
        myrOnlyColorless = Math.max(0, myrOnlyColorless - amount);
    }

    public int getRestrictedRed() {
        return restrictedRed;
    }

    public void addRestrictedRed(int amount) {
        restrictedRed += amount;
    }

    public void removeRestrictedRed(int amount) {
        restrictedRed = Math.max(0, restrictedRed - amount);
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.getOrDefault(color, 0);
            if (color == ManaColor.COLORLESS) {
                amount += artifactOnlyColorless + myrOnlyColorless;
            }
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            map.put(color.getCode(), amount);
        }
        return map;
    }
}
