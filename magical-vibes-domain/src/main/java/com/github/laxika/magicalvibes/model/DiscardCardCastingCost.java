package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * A casting-cost component requiring the player to discard cards from their hand.
 */
public record DiscardCardCastingCost(CardPredicate predicate, String label, int count) implements CastingCost {

    public DiscardCardCastingCost {
        if (count < 1) {
            throw new IllegalArgumentException("discard count must be >= 1");
        }
    }

    public DiscardCardCastingCost() {
        this(null, null, 1);
    }

    public DiscardCardCastingCost(CardPredicate predicate, String label) {
        this(predicate, label, 1);
    }
}
