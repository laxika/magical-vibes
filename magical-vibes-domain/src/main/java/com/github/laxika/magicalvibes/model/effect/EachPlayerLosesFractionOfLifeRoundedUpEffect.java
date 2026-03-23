package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player loses 1/divisor of their life total, rounded up.
 * Example: divisor=3 → each player loses a third of their life, rounded up.
 */
public record EachPlayerLosesFractionOfLifeRoundedUpEffect(int divisor) implements CardEffect {
}
