package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManaPool {

    private final EnumMap<ManaColor, Integer> pool = new EnumMap<>(ManaColor.class);
    private final EnumMap<ManaColor, Integer> creatureMana = new EnumMap<>(ManaColor.class);
    /** Mana that doesn't drain at step/phase transitions until end of turn (e.g. Grand Warlord Radha). */
    private final EnumMap<ManaColor, Integer> persistentMana = new EnumMap<>(ManaColor.class);
    private int artifactOnlyColorless;
    private int myrOnlyColorless;
    private int restrictedRed;
    private int kickedOnlyGreen;
    private int instantSorceryOnlyColorless;

    public ManaPool() {
        for (ManaColor color : ManaColor.values()) {
            pool.put(color, 0);
            creatureMana.put(color, 0);
            persistentMana.put(color, 0);
        }
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     */
    public ManaPool(ManaPool source) {
        pool.putAll(source.pool);
        creatureMana.putAll(source.creatureMana);
        persistentMana.putAll(source.persistentMana);
        this.artifactOnlyColorless = source.artifactOnlyColorless;
        this.myrOnlyColorless = source.myrOnlyColorless;
        this.restrictedRed = source.restrictedRed;
        this.kickedOnlyGreen = source.kickedOnlyGreen;
        this.instantSorceryOnlyColorless = source.instantSorceryOnlyColorless;
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
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless = 0;
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

    public int getKickedOnlyGreen() {
        return kickedOnlyGreen;
    }

    public void addKickedOnlyGreen(int amount) {
        kickedOnlyGreen += amount;
    }

    public void removeKickedOnlyGreen(int amount) {
        kickedOnlyGreen = Math.max(0, kickedOnlyGreen - amount);
    }

    public int getInstantSorceryOnlyColorless() {
        return instantSorceryOnlyColorless;
    }

    public void addInstantSorceryOnlyColorless(int amount) {
        instantSorceryOnlyColorless += amount;
    }

    public void removeInstantSorceryOnlyColorless(int amount) {
        instantSorceryOnlyColorless = Math.max(0, instantSorceryOnlyColorless - amount);
    }

    /**
     * Adds mana that persists through step/phase transitions until end of turn.
     * The mana is added to both the regular pool and the persistent tracker.
     */
    public void addPersistentMana(ManaColor color, int amount) {
        pool.merge(color, amount, Integer::sum);
        persistentMana.merge(color, amount, Integer::sum);
    }

    /**
     * Drains all non-persistent mana. For each color, the pool is reduced to
     * at most the persistent amount. Persistent mana survives step/phase transitions.
     */
    public void drainNonPersistent() {
        for (ManaColor color : ManaColor.values()) {
            int persistent = persistentMana.getOrDefault(color, 0);
            int current = pool.getOrDefault(color, 0);
            // Keep the lesser of current pool and persistent amount
            pool.put(color, Math.min(current, persistent));
            // Clamp persistent to not exceed what's in the pool
            persistentMana.put(color, Math.min(current, persistent));
        }
        // Clamp creature mana to not exceed pool totals
        for (ManaColor color : ManaColor.values()) {
            int total = pool.getOrDefault(color, 0);
            int creature = creatureMana.getOrDefault(color, 0);
            creatureMana.put(color, Math.min(creature, total));
        }
        artifactOnlyColorless = 0;
        myrOnlyColorless = 0;
        restrictedRed = 0;
        kickedOnlyGreen = 0;
        instantSorceryOnlyColorless = 0;
    }

    /**
     * Clears all persistent mana tracking. Called during end-of-turn cleanup
     * so subsequent drains will empty the pool normally.
     */
    public void clearPersistentMana() {
        for (ManaColor color : ManaColor.values()) {
            persistentMana.put(color, 0);
        }
    }

    public int getPersistentMana(ManaColor color) {
        return persistentMana.getOrDefault(color, 0);
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.getOrDefault(color, 0);
            if (color == ManaColor.COLORLESS) {
                amount += artifactOnlyColorless + myrOnlyColorless + instantSorceryOnlyColorless;
            }
            if (color == ManaColor.RED) {
                amount += restrictedRed;
            }
            if (color == ManaColor.GREEN) {
                amount += kickedOnlyGreen;
            }
            map.put(color.getCode(), amount);
        }
        return map;
    }
}
