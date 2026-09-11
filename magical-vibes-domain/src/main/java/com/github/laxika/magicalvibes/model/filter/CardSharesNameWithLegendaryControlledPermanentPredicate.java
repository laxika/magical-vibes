package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a legendary card whose name is shared by a legendary permanent controlled by the
 * perspective player. Requires the game-data overload of card-predicate evaluation.
 */
public record CardSharesNameWithLegendaryControlledPermanentPredicate() implements CardPredicate {
}
