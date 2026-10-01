package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of cards matching {@code predicate} in the libraries of the players in scope. */
public record MatchingCardsInLibrary(CountScope scope, CardPredicate predicate) implements DynamicAmount {
}
