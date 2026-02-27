package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect on a permanent that reduces the casting cost of its controller's spells
 * by the given amount if the spell shares a card type with the card imprinted on this permanent.
 * Used by Semblance Anvil.
 */
public record ReduceOwnCastCostForSharedCardTypeWithImprintEffect(int amount) implements CardEffect {
}
