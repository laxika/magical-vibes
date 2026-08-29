package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the relevant player's library. If it matches {@code predicate}, that
 * player may put it onto the battlefield with a counter and an additional card type.
 */
public record RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect(
        CardPredicate predicate, CounterType counterType, CardType addedCardType) implements CardEffect {
}
