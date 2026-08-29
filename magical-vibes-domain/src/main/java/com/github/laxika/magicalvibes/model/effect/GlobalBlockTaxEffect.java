package com.github.laxika.magicalvibes.model.effect;

/**
 * Floating, turn-scoped global block tax created by an effect such as War Cadence.
 */
public record GlobalBlockTaxEffect(int blockCostPerCreature) implements GlobalBlockCostEffect {
}
