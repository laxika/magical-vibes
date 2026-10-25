package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects which actually destroyed permanents a {@link DestroyAllPermanentsEffect} exposes as
 * its destroyed-count event value. A permanent that was destroyed but that a replacement (Rest in
 * Peace, Kalitas, Wheel of Sun and Moon) put somewhere other than a graveyard still counts as
 * "destroyed this way" (Fumigate and Death Begets Life rulings), except for {@link #DIED}.
 */
public enum DestroyedPermanentCountScope {
    /** Count every permanent actually destroyed. */
    ALL,
    /** Count every nontoken permanent actually destroyed. */
    ALL_NONTOKEN,
    /** Count permanents actually destroyed that were controlled by the spell's controller. */
    CONTROLLER,
    /** Count nontoken permanents actually destroyed that were controlled by the spell's controller. */
    CONTROLLER_NONTOKEN,
    /**
     * Count every permanent that died this way: destroyed and put into a graveyard ("for each creature
     * that died this way", Hellfire, Mogg Infestation).
     */
    DIED
}
