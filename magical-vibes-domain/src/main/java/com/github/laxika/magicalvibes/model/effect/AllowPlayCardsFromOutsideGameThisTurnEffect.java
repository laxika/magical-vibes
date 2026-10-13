package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller permission to play matching cards from outside the game this turn.
 * {@code maxCards} limits how many matching cards this resolution grants (for example "play one
 * basic Plains card"); cards already granted by an earlier resolution don't count against it.
 */
public record AllowPlayCardsFromOutsideGameThisTurnEffect(CardPredicate filter, int maxCards)
        implements CardEffect {

    /** Grants permission for every matching card in the controller's outside-the-game card pool. */
    public AllowPlayCardsFromOutsideGameThisTurnEffect(CardPredicate filter) {
        this(filter, Integer.MAX_VALUE);
    }

    /** Grants permission for every card in the controller's outside-the-game card pool. */
    public AllowPlayCardsFromOutsideGameThisTurnEffect() {
        this(null, Integer.MAX_VALUE);
    }
}
