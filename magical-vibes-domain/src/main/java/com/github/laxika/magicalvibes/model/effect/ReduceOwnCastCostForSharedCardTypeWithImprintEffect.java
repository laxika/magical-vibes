package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Static effect on a permanent that reduces the casting cost of its controller's spells
 * by the evaluated {@link DynamicAmount} if the spell shares a card type with the card
 * imprinted on this permanent. Used by Semblance Anvil.
 */
public record ReduceOwnCastCostForSharedCardTypeWithImprintEffect(DynamicAmount amount) implements CardEffect {
}
