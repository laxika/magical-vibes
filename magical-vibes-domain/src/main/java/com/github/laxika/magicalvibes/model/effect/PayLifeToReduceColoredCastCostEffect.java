package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect that lets the applicable caster pay life instead of the colored mana reduction
 * granted to a matching spell.
 */
public record PayLifeToReduceColoredCastCostEffect(
        CardPredicate spellFilter,
        String reductionManaCost,
        int lifeAmount,
        CostModificationScope scope
) implements CardEffect {
}
