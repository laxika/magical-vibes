package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * revealed. That card goes to {@code destination} ({@link LibrarySearchDestination#BATTLEFIELD} or
 * {@link LibrarySearchDestination#HAND}), and the other revealed cards are put on the bottom of the
 * library in a random order.
 *
 * @param enterTappedAndAttacking if true, a battlefield card enters tapped and attacking after its
 *                                controller chooses a legal attack destination
 * @param recordFoundCardManaValue if true, the found card's mana value is recorded as the stack
 *                                 entry's event value, or zero when no matching card is found
 * @param recordRevealedCount if true, stores the number of revealed cards in the stack entry's
 *                            event value for a following effect to use
 */
public record RevealUntilCardPredicateRestOnBottomRandomEffect(
        CardPredicate predicate,
        LibrarySearchDestination destination,
        boolean enterTappedAndAttacking,
        boolean recordRevealedCount,
        boolean recordFoundCardManaValue
) implements CardEffect {

    public RevealUntilCardPredicateRestOnBottomRandomEffect(
            CardPredicate predicate, LibrarySearchDestination destination) {
        this(predicate, destination, false, false, false);
    }

    public RevealUntilCardPredicateRestOnBottomRandomEffect(
            CardPredicate predicate, LibrarySearchDestination destination,
            boolean enterTappedAndAttacking) {
        this(predicate, destination, enterTappedAndAttacking, false, false);
    }

    public RevealUntilCardPredicateRestOnBottomRandomEffect(
            CardPredicate predicate, LibrarySearchDestination destination,
            boolean enterTappedAndAttacking, boolean recordRevealedCount) {
        this(predicate, destination, enterTappedAndAttacking, recordRevealedCount, false);
    }

    /** Records the found card's mana value for a following {@link EventValue} amount. */
    public static RevealUntilCardPredicateRestOnBottomRandomEffect toHandRecordingManaValue(
            CardPredicate predicate) {
        return new RevealUntilCardPredicateRestOnBottomRandomEffect(
                predicate, LibrarySearchDestination.HAND, false, false, true);
    }

    public static RevealUntilCardPredicateRestOnBottomRandomEffect tappedAndAttacking(
            CardPredicate predicate) {
        return new RevealUntilCardPredicateRestOnBottomRandomEffect(
                predicate, LibrarySearchDestination.BATTLEFIELD, true, false);
    }
}
