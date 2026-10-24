package com.github.laxika.magicalvibes.model;

/**
 * The stage a {@link DepartureBatch} has reached. Only {@link #GATHER} and {@link #CHOOSE} can wait
 * for a player's answer; a pending batch resumes at the stage where it stopped.
 */
public enum DepartureStage {
    /** Destruction replacements (indestructible, regeneration, shield counters) for destroyed objects. */
    GATHER,
    /** CR 616.1 death replacements for every object still leaving, asked in APNAP order (CR 101.4). */
    CHOOSE,
    /** Every object still leaving leaves together, as one event. */
    PERFORM,
    /** A {@link DepartureResult} is reported for every intent. */
    REPORT
}
