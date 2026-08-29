package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each player other than the resolving stack entry's controller may return up to
 * {@code maxCount} matching cards from their graveyard to the battlefield under their control.
 * Players choose independently in APNAP order.
 */
public record EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect(
        int maxCount,
        CardPredicate filter
) implements CardEffect {
}
