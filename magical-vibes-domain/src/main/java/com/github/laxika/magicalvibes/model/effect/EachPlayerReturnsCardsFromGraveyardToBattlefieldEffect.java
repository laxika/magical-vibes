package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each player returns up to {@code maxCount} cards matching {@code filter} from their graveyard
 * to the battlefield. Players choose in APNAP order. If a player has fewer matching cards than
 * {@code maxCount}, they return all of them. When {@code enterWithCounter} is non-null, each
 * card returned this way enters with one counter of that type on it (auto-return path only).
 *
 * <p>Example: "Each player returns up to two land cards from their graveyard to the battlefield."
 * → {@code new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, new CardTypePredicate(CardType.LAND))}
 *
 * <p>Example (Pyrrhic Revival): "Each player returns each creature card from their graveyard to
 * the battlefield with an additional -1/-1 counter on it."
 * → {@code new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(Integer.MAX_VALUE,
 *   new CardTypePredicate(CardType.CREATURE), CounterType.MINUS_ONE_MINUS_ONE)}
 */
public record EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
        int maxCount,
        CardPredicate filter,
        CounterType enterWithCounter
) implements CardEffect {

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter) {
        this(maxCount, filter, null);
    }
}
