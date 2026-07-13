package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: all damage is dealt as though its source had wither (CR 702.80).
 * Global — applies to every damage source on any battlefield. Damage to creatures is
 * dealt as -1/-1 counters; damage to players remains normal life loss (wither, not infect).
 * Used by Everlasting Torment.
 */
public record AllDamageDealtWithWitherEffect() implements CardEffect {
}
