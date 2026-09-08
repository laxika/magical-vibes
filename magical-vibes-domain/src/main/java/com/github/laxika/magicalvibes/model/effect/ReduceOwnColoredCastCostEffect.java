package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reduces this spell's own cost by the evaluated number of matching colored mana symbols, then
 * generic mana if the colored component is exhausted.
 */
public record ReduceOwnColoredCastCostEffect(ManaColor color, DynamicAmount amount) implements CardEffect {
}
