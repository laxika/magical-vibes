package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the power of each other creature controlled by the effect's controller until end of
 * turn. Each creature's current effective power is added to that creature independently when the
 * effect resolves.
 */
public record DoubleOtherOwnCreaturesPowerEffect() implements CardEffect {
}
