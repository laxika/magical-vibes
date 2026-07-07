package com.github.laxika.magicalvibes.model.effect;

/**
 * A last-known statistic of the permanent destroyed by a {@link DestroyTargetPermanentThenEffect},
 * snapshotted onto {@code StackEntry.eventValue} <em>before</em> the permanent leaves the battlefield
 * so the rider can read it via the {@code EventValue} amount (CR 608.2h last-known information).
 */
public enum EventStat {
    /** No stat is snapshotted (the rider's amount is self-contained). */
    NONE,
    /** The destroyed permanent's mana value (Divine Offering, Hoard-Smelter Dragon). */
    MANA_VALUE,
    /** The destroyed creature's effective toughness (Death's Caress, Engulfing Slagwurm). */
    TOUGHNESS
}
