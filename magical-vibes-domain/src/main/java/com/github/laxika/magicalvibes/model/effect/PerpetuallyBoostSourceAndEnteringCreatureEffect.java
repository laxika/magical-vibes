package com.github.laxika.magicalvibes.model.effect;

/** Perpetually gives the source and the creature that caused its enter trigger a power/toughness boost. */
public record PerpetuallyBoostSourceAndEnteringCreatureEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
