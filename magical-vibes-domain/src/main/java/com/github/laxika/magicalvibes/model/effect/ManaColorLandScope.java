package com.github.laxika.magicalvibes.model.effect;

/**
 * Which players' lands are scanned by {@link AwardManaOfColorsLandsCouldProduceEffect} when
 * determining which mana colors are available.
 */
public enum ManaColorLandScope {
    /** Lands the ability's controller controls (e.g. Star Compass — "a basic land you control"). */
    CONTROLLER,
    /** Lands an opponent controls (e.g. Fellwar Stone — "a land an opponent controls"). */
    OPPONENTS
}
