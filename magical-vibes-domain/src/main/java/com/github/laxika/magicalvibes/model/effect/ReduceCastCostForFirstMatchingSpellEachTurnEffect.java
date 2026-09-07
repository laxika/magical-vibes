package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: the first spell cast by the source controller each turn that matches the
 * predicate costs the given amount less generic mana.
 */
public record ReduceCastCostForFirstMatchingSpellEachTurnEffect(CardPredicate predicate, int amount)
        implements CardEffect {
}
