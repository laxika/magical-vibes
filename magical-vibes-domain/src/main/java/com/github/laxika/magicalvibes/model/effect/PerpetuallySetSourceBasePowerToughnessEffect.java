package com.github.laxika.magicalvibes.model.effect;

/** Perpetually sets the source card's base power and toughness. */
public record PerpetuallySetSourceBasePowerToughnessEffect(int power, int toughness)
        implements CardEffect {
}
