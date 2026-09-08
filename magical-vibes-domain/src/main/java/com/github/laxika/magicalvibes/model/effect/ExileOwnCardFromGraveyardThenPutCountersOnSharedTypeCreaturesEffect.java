package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles one matching card from the controller's graveyard, then puts counters on each other
 * creature that shares a creature type with the exiled card.
 */
public record ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect(
        CardPredicate filter,
        CounterType counterType,
        int counterCount,
        String cardDescription
) implements CardEffect {
}
