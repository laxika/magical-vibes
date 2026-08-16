package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Creates a temporary reduction for matching spells cast by the effect's controller.
 */
public record ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate, int amount)
        implements CardEffect {
}
