package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for "Damage isn't removed from creatures during cleanup steps." It is an ability of
 * its source (a Saga that gained it), so it applies to every creature regardless of the creatures'
 * own abilities.
 */
public record DamageNotRemovedFromAllCreaturesDuringCleanupEffect() implements CardEffect {
}
