package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Reveals cards from the top of a library (the controller's, or that of each player {@code revealedBy} names) until
 * a card matching the predicate is revealed. That card goes to {@code destination} ({@link LibrarySearchDestination#BATTLEFIELD} or
 * {@link LibrarySearchDestination#HAND}), and the other revealed cards are put on the bottom of the
 * library in a random order, or shuffled into it ({@code shuffleRest}).
 *
 * @param enterTappedAndAttacking if true, a battlefield card enters tapped and attacking after its
 *                                controller chooses a legal attack destination
 * @param recordFoundCardManaValue if true, the found card's mana value is recorded as the stack
 *                                 entry's event value, or zero when no matching card is found
 * @param recordRevealedCount if true, stores the number of revealed cards in the stack entry's
 *                            event value for a following effect to use
 * @param shuffleRest "then shuffles the rest into their library"
 * @param revealedBy who reveals, from their own library; {@code null} for the controller
 */
public record RevealUntilCardPredicateRestOnBottomRandomEffect(
        CardPredicate predicate,
        LibrarySearchDestination destination,
        boolean enterTappedAndAttacking,
        boolean recordRevealedCount,
        boolean recordFoundCardManaValue,
        boolean shuffleRest,
        Revealers revealedBy
) implements CardEffect {

    /** Who reveals, when it isn't the controller. */
    public sealed interface Revealers {
    }

    /** "Each player reveals ...", "its controller reveals ...": the players at once, what they find entering together. */
    public record Players(PlayerSubject players) implements Revealers {

        public Players {
            if (players.declaresTarget()) {
                throw new IllegalArgumentException("The reveal names its players without a target: " + players);
            }
        }
    }

    /**
     * "For each creature exiled this way, its controller reveals ...": one reveal per permanent of the exile step
     * before it, one after another, in turn order from the active player (Divergent Transformations' rulings).
     */
    public record ControllerOfEachExiledThisWay() implements Revealers {
    }

    /** The reveals left of a {@link ControllerOfEachExiledThisWay}, each by that player. */
    public record InOrder(List<UUID> playerIds) implements Revealers {

        public InOrder {
            playerIds = List.copyOf(playerIds);
        }
    }

    public RevealUntilCardPredicateRestOnBottomRandomEffect(
            CardPredicate predicate, LibrarySearchDestination destination, boolean enterTappedAndAttacking,
            boolean recordRevealedCount, boolean recordFoundCardManaValue) {
        this(predicate, destination, enterTappedAndAttacking, recordRevealedCount, recordFoundCardManaValue,
                false, null);
    }

    /** "[Players] reveal ... until they reveal a [card], put it onto the battlefield". */
    public static RevealUntilCardPredicateRestOnBottomRandomEffect ontoBattlefield(CardPredicate predicate,
                                                                                   Revealers revealedBy) {
        return new RevealUntilCardPredicateRestOnBottomRandomEffect(
                predicate, LibrarySearchDestination.BATTLEFIELD, false, false, false, false, revealedBy);
    }

    /** "... then shuffle the rest into their library." */
    public RevealUntilCardPredicateRestOnBottomRandomEffect shufflingRest() {
        return new RevealUntilCardPredicateRestOnBottomRandomEffect(predicate, destination, enterTappedAndAttacking,
                recordRevealedCount, recordFoundCardManaValue, true, revealedBy);
    }

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
