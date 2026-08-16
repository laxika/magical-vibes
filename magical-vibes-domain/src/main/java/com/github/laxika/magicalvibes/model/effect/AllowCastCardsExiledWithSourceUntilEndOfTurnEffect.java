package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller one temporary permission to cast a matching card exiled with the source
 * permanent until end of turn.
 *
 * <p>The permission is source-linked, but the activated ability that creates it remains independent
 * of the source permanent. Casting one card consumes the grant; activating the ability again creates
 * another grant.</p>
 */
public record AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
        CardPredicate filter,
        boolean withoutPayingManaCost
) implements CardEffect {
}
