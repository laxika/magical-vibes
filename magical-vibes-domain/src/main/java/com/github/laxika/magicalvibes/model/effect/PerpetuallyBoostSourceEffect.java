package com.github.laxika.magicalvibes.model.effect;

/** Perpetually modifies the source card's printed power and toughness. */
public record PerpetuallyBoostSourceEffect(int powerBoost, int toughnessBoost) implements CardEffect {
}
