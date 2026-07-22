package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of non-token cards matching the predicate in the exile zone(s) in scope (by owner). */
public record CardsInExile(CardPredicate filter, CountScope scope) implements DynamicAmount {
}
