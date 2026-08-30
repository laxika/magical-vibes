package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's outside-the-game card pool for a card to put into hand. */
public record SearchOutsideGameToHandEffect(CardPredicate filter) implements CardEffect {

    /** Searches for any card. */
    public SearchOutsideGameToHandEffect() {
        this(null);
    }
}
