package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces the matching spells' cost by a dynamic number of the given colored mana symbol, using
 * generic mana once those colored symbols are exhausted.
 */
public record ReduceColoredCastCostForMatchingSpellsByAmountEffect(
        CardPredicate predicate,
        ManaColor color,
        DynamicAmount amount,
        CostModificationScope scope
) implements CardEffect {
}
