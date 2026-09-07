package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * revealed. That card goes to {@code destination} ({@link LibrarySearchDestination#BATTLEFIELD} or
 * {@link LibrarySearchDestination#HAND}), and the other revealed cards are put on the bottom of the
 * library in a random order.
 *
 * @param enterTappedAndAttacking if true, a battlefield card enters tapped and attacking after its
 *                                controller chooses a legal attack destination
 */
public record RevealUntilCardPredicateRestOnBottomRandomEffect(
        CardPredicate predicate,
        LibrarySearchDestination destination,
        boolean enterTappedAndAttacking
) implements CardEffect {

    public RevealUntilCardPredicateRestOnBottomRandomEffect(
            CardPredicate predicate, LibrarySearchDestination destination) {
        this(predicate, destination, false);
    }

    public static RevealUntilCardPredicateRestOnBottomRandomEffect tappedAndAttacking(
            CardPredicate predicate) {
        return new RevealUntilCardPredicateRestOnBottomRandomEffect(
                predicate, LibrarySearchDestination.BATTLEFIELD, true);
    }
}
