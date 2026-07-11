package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of non-token cards matching the predicate in the graveyard(s) in scope. */
public record CardsInGraveyard(CardPredicate filter, CountScope scope) implements DynamicAmount {
}
