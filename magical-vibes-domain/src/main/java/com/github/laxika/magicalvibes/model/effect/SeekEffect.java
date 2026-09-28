package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts a random card matching the predicate from the controller's library into their hand. */
public record SeekEffect(CardPredicate predicate, boolean storeSelectedCard) implements CardEffect {

    public SeekEffect(CardPredicate predicate) {
        this(predicate, false);
    }
}
