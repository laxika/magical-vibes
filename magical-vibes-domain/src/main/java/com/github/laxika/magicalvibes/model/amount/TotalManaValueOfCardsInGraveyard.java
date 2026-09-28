package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The total mana value of matching non-token cards in the scoped graveyard(s). */
public record TotalManaValueOfCardsInGraveyard(CardPredicate filter, CountScope scope)
        implements DynamicAmount {
}
