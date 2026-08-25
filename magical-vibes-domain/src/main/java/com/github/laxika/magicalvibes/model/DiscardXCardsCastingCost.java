package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Flashback casting-cost component requiring the caster to discard the announced number of cards.
 */
public record DiscardXCardsCastingCost(CardPredicate predicate, String label) implements CastingCost {

    public DiscardXCardsCastingCost() {
        this(null, null);
    }
}
