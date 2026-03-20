package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each player returns up to {@code maxCount} cards matching {@code filter} from their graveyard
 * to the battlefield. Players choose in APNAP order. If a player has fewer matching cards than
 * {@code maxCount}, they return all of them.
 *
 * <p>Example: "Each player returns up to two land cards from their graveyard to the battlefield."
 * → {@code new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, new CardTypePredicate(CardType.LAND))}
 */
public record EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
        int maxCount,
        CardPredicate filter
) implements CardEffect {
}
