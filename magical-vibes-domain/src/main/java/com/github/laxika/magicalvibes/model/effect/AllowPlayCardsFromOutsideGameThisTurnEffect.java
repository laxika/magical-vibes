package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Grants the controller permission to play matching cards from outside the game this turn. */
public record AllowPlayCardsFromOutsideGameThisTurnEffect(CardPredicate filter) implements CardEffect {

    /** Grants permission for every card in the controller's outside-the-game card pool. */
    public AllowPlayCardsFromOutsideGameThisTurnEffect() {
        this(null);
    }
}
