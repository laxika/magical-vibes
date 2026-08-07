package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * revealed. That card goes to {@code destination} ({@link LibrarySearchDestination#BATTLEFIELD} or
 * {@link LibrarySearchDestination#HAND}), and the other revealed cards are put on the bottom of the
 * library in a random order.
 */
public record RevealUntilCardPredicateRestOnBottomRandomEffect(
        CardPredicate predicate,
        LibrarySearchDestination destination
) implements CardEffect {
}
