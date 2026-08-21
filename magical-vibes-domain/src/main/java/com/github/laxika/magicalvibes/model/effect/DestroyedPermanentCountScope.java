package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects which actually destroyed permanents a {@link DestroyAllPermanentsEffect} exposes as
 * its destroyed-count event value.
 */
public enum DestroyedPermanentCountScope {
    ALL,
    CONTROLLER
}
