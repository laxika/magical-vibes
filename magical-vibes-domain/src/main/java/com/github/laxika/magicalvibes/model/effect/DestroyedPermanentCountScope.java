package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects which actually destroyed permanents a {@link DestroyAllPermanentsEffect} exposes as
 * its destroyed-count event value.
 */
public enum DestroyedPermanentCountScope {
    /** Count every permanent actually destroyed. */
    ALL,
    /** Count permanents actually destroyed that were controlled by the spell's controller. */
    CONTROLLER,
    /** Count nontoken permanents actually destroyed that were controlled by the spell's controller. */
    CONTROLLER_NONTOKEN
}
