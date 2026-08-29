package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller permission to cast every matching card exiled with the source permanent
 * until end of turn, paying normal costs.
 */
public record AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter)
        implements CardEffect {
}
