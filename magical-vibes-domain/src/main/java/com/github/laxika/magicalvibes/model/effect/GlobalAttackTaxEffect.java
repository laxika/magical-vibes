package com.github.laxika.magicalvibes.model.effect;

/**
 * Floating, turn-scoped global attack tax created by an effect such as War Tax.
 */
public record GlobalAttackTaxEffect(int attackCostPerCreature) implements GlobalAttackCostEffect {
}
