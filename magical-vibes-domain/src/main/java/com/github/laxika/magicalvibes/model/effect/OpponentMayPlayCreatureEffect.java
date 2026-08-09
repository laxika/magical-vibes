package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the opponent of the effect's controller put a matching creature card from hand onto the
 * battlefield. The no-argument form is the original unrestricted-creature variant.
 */
public record OpponentMayPlayCreatureEffect(CardPredicate predicate, String label) implements CardEffect {

    public OpponentMayPlayCreatureEffect() {
        this(null, "creature");
    }
}
