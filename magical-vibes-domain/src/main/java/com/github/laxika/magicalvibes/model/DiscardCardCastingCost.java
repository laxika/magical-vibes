package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * A casting-cost component requiring the player to discard one card from their hand.
 */
public record DiscardCardCastingCost(CardPredicate predicate, String label) implements CastingCost {

    public DiscardCardCastingCost() {
        this(null, null);
    }
}
