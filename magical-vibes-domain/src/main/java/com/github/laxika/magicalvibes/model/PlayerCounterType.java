package com.github.laxika.magicalvibes.model;

/**
 * The kinds of counters a player can have that the engine tracks (CR 122.1), each kept in its own {@link GameData}
 * store: {@link GameData#playerCounterCount} reads one, {@link GameData#removeAllPlayerCounters} takes them all away.
 */
public enum PlayerCounterType {
    POISON,
    ENERGY,
    EXPERIENCE,
    RAD,
    SPARK
}
