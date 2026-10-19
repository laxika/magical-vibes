package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller permission to cast every matching card exiled with the source permanent
 * until end of turn. When {@code exiledThisResolutionOnly} is set, only the cards exiled by the
 * resolving ability itself ("you may play that card") qualify, not earlier exiles with the source.
 */
public record AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(
        CardPredicate filter,
        boolean withoutPayingManaCost,
        boolean exiledThisResolutionOnly
) implements CardEffect {

    public AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter, boolean withoutPayingManaCost) {
        this(filter, withoutPayingManaCost, false);
    }

    public AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter) {
        this(filter, false, false);
    }
}
