package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Boosts the creature identified by {@code scope} by +X/+X, where X is
 * the number of cards in all graveyards matching the given predicate.
 * Used by cards like Bonehoard (with a creature card predicate).
 */
public record BoostCreaturePerCardsInAllGraveyardsEffect(
        CardPredicate filter,
        GrantScope scope
) implements CardEffect {
}
