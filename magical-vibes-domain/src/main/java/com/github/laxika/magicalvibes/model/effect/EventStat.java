package com.github.laxika.magicalvibes.model.effect;

/**
 * A last-known statistic of a permanent affected by a removal effect, snapshotted onto
 * {@code StackEntry.eventValue} <em>before</em> the permanent leaves the battlefield so a rider can
 * read it via the {@code EventValue} amount.
 */
public enum EventStat {
    /** No stat is snapshotted (the rider's amount is self-contained). */
    NONE,
    /** The destroyed permanent's mana value (Divine Offering, Hoard-Smelter Dragon). */
    MANA_VALUE,
    /** The destroyed creature's effective toughness (Death's Caress, Engulfing Slagwurm). */
    TOUGHNESS,
    /** The destroyed creature's effective power, clamped to at least 0 (Cinder Cloud). */
    POWER
}
