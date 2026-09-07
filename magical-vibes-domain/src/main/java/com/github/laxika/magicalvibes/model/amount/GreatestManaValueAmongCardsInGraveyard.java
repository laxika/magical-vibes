package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The greatest mana value among matching non-token cards in the scoped graveyard(s). */
public record GreatestManaValueAmongCardsInGraveyard(CardPredicate filter, CountScope scope)
        implements DynamicAmount {
}
