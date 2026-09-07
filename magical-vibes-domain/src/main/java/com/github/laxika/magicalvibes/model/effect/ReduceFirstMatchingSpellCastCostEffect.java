package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reduces the first matching spell cast by the source controller during their turn by generic mana. */
public record ReduceFirstMatchingSpellCastCostEffect(CardPredicate predicate, int amount)
        implements CardEffect {
}
