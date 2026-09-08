package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player loses 1/divisor of their life total, rounded down.
 */
public record EachPlayerLosesFractionOfLifeRoundedDownEffect(int divisor) implements CardEffect {
}
