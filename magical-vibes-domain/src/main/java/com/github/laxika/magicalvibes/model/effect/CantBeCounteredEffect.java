package com.github.laxika.magicalvibes.model.effect;

/**
 * "This spell can't be countered."
 * Static ability on the card — checked during counter-spell resolution.
 * Registered in {@code EffectSlot.STATIC}.
 */
public record CantBeCounteredEffect() implements CardEffect {
}
