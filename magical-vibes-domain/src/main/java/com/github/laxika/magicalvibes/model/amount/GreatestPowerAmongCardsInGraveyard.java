package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The greatest printed power among matching non-token cards in the scoped graveyard(s). */
public record GreatestPowerAmongCardsInGraveyard(CardPredicate filter, CountScope scope) implements DynamicAmount {
}
