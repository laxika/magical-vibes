package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A player drew a card (CR 121.1), revealed a drawn card by an ability, discarded a card (CR 701.9a) or cycled one
 * (CR 702.29a), scried (CR 701.22a), surveilled (CR 701.25a), searched their library (CR 701.23a) or had it shuffled
 * (CR 701.24a): the events "whenever you draw a card", "whenever an opponent discards a card", "whenever you scry" and
 * their kin trigger on (CR 603.2). Each is an occurrence of its own, emitted where the player does it.
 */
public sealed interface LibraryEvent extends RulesEvent {

    /** The player who drew, revealed, discarded, cycled, scried, surveilled or searched, or whose library was shuffled. */
    UUID playerId();

    /**
     * {@code playerId} drew {@code card}.
     *
     * @param card               the card drawn, or {@code null} for a draw no card is known of
     * @param firstInDrawStep    whether it is the player's first draw in their own draw step
     * @param cardsDrawnThisTurn how many cards the player has drawn this turn, this one included
     */
    record Drew(UUID playerId, Card card, boolean firstInDrawStep, int cardsDrawnThisTurn) implements LibraryEvent {

        public Drew {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Drew deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId} revealed {@code card}, a card they just drew, by {@code source}'s reveal ability: the event of
     * {@code source}'s own "whenever you reveal a [card] this way" abilities.
     */
    record RevealedDrawnCard(UUID playerId, Permanent source, Card card) implements LibraryEvent {

        public RevealedDrawnCard {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(card, "card");
        }

        @Override
        public RevealedDrawnCard deepCopy() {
            return new RevealedDrawnCard(playerId, new Permanent(source), card);
        }
    }

    /**
     * {@code playerId} discarded {@code card}; cycling discards the card (CR 702.29a).
     *
     * @param card             the card discarded, or {@code null} for a discard no card is known of
     * @param cycled           whether it was discarded to pay a cycling cost
     * @param causedByOpponent whether a spell or ability an opponent of the player controls caused the discard
     * @param alone            whether the discard is a discard event of its own ("whenever you discard one or more
     *                         cards"), not one card of a {@link DiscardedCards} event
     */
    record Discarded(UUID playerId, Card card, boolean cycled, boolean causedByOpponent, boolean alone)
            implements LibraryEvent {

        public Discarded {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Discarded deepCopy() {
            return this;
        }
    }

    /** {@code playerId} discarded {@code cards} at once, one discard event of one or more cards. */
    record DiscardedCards(UUID playerId, List<Card> cards) implements LibraryEvent {

        public DiscardedCards {
            Objects.requireNonNull(playerId, "playerId");
            cards = List.copyOf(cards);
        }

        @Override
        public DiscardedCards deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId} cycled {@code card} (CR 702.29a), after its discard.
     *
     * @param xValue                  the X paid for the cycling cost (Shark Typhoon)
     * @param targetingCyclingAbility the cycling ability that announced the targets of the card's own "when you cycle
     *                                this card" ability (Rampaging War Mammoth), or {@code null}
     * @param announcedTargetIds      the targets that ability announced, or {@code null}
     */
    record Cycled(UUID playerId, Card card, int xValue, ActivatedAbility targetingCyclingAbility,
                  List<UUID> announcedTargetIds) implements LibraryEvent {

        public Cycled {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(card, "card");
            announcedTargetIds = announcedTargetIds == null ? null : List.copyOf(announcedTargetIds);
        }

        /** {@code playerId} cycled {@code card}, its cycling ability announcing no targets. */
        public Cycled(UUID playerId, Card card) {
            this(playerId, card, 0, null, null);
        }

        @Override
        public Cycled deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId} scried.
     *
     * @param bottomedCardCount how many cards they put on the bottom of their library
     * @param cardsLookedAt     how many cards they looked at
     */
    record Scried(UUID playerId, int bottomedCardCount, int cardsLookedAt) implements LibraryEvent {

        public Scried {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Scried deepCopy() {
            return this;
        }
    }

    /** {@code playerId} surveilled, for the first time this turn when {@code firstThisTurn}. */
    record Surveilled(UUID playerId, boolean firstThisTurn) implements LibraryEvent {

        public Surveilled {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Surveilled deepCopy() {
            return this;
        }
    }

    /** {@code playerId} searched their library. */
    record Searched(UUID playerId) implements LibraryEvent {

        public Searched {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Searched deepCopy() {
            return this;
        }
    }

    /**
     * {@code playerId}'s library was shuffled.
     *
     * @param causingControllerId the controller of the spell or ability that caused the shuffle, or {@code null} when
     *                            none was resolving
     */
    record Shuffled(UUID playerId, UUID causingControllerId) implements LibraryEvent {

        public Shuffled {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Shuffled deepCopy() {
            return this;
        }
    }

    /** {@code playerId} searched their library. */
    static LibraryEvent searched(UUID playerId) {
        return new Searched(playerId);
    }

    /** {@code playerId}'s library was shuffled, by a spell or ability {@code causingControllerId} controls. */
    static LibraryEvent shuffled(UUID playerId, UUID causingControllerId) {
        return new Shuffled(playerId, causingControllerId);
    }
}
