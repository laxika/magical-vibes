package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller permission to cast every matching card exiled with the source permanent
 * until end of turn.
 */
public record AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(
        CardPredicate filter,
        boolean withoutPayingManaCost
) implements CardEffect {

    public AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter) {
        this(filter, false);
    }
}
