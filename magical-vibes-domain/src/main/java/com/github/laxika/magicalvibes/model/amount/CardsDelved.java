package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of cards exiled to pay the resolving spell's delve cost that match the filter. */
public record CardsDelved(CardPredicate filter) implements DynamicAmount {
}
