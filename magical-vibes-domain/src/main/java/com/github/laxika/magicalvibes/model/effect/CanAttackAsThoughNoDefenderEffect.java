package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can attack as though it didn't have defender.
 * Typically wrapped in a conditional effect (e.g. MetalcraftConditionalEffect)
 * so the creature can only attack when the condition is met.
 */
public record CanAttackAsThoughNoDefenderEffect() implements CardEffect {
}
