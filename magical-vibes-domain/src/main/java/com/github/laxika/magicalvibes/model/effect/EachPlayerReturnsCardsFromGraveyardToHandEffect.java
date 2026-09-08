package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns up to {@code maxCount} matching cards from each player's graveyard to that player's
 * hand. Players with more matching cards choose in APNAP order.
 *
 * @param maxCount maximum number of cards each player may return
 * @param filter cards that may be returned
 */
public record EachPlayerReturnsCardsFromGraveyardToHandEffect(
        int maxCount,
        CardPredicate filter
) implements CardEffect {
}
