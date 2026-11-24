package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put the rest on the bottom of their owners' libraries", "put that card on top of its owner's library", "puts all
 * the cards from their graveyard on the bottom of their library in a random order": each card {@code cards} names
 * that is in a graveyard, in exile or in a hand, or still revealed in that library (CR 701.20b:
 * {@code Subjects.revealedThisWay()}, "the rest on the bottom"), goes there, all as one event, each owner ordering
 * their own unless the order is random (CR 401.4). A card the subject finds in another zone stays there. Records
 * {@code StepKind.PUT_INTO_LIBRARY}: "if you do, draw a card", "then draw that many cards".
 *
 * <p>"Shuffle it into its owner's library", "you shuffle up to four cards from your graveyard into your library" is
 * {@link #shuffledIn}: the cards go into their owners' libraries, each of which is then shuffled once, and the step
 * records {@code StepKind.SHUFFLE_INTO_LIBRARY} instead (how many each owner shuffled in: "then draw that many
 * cards", "if you do"). A library none of the cards went into is shuffled only when {@code alwaysShuffled} names its
 * player: the text names a specific object or a set that may be empty (CR 701.24c, 701.24d); a choice that was
 * impossible shuffles nothing (See Beyond's and Lat-Nam's Legacy's rulings).
 *
 * @param cards          the cards put into the library
 * @param onTop          on top; otherwise on the bottom (unused when shuffled in)
 * @param randomOrder    "in a random order" (unused when shuffled in)
 * @param shuffled       shuffled into the library rather than put in a position
 * @param alwaysShuffled when shuffled in, the players whose library is shuffled even if none of the cards went into
 *                       it; {@code null} for none
 */
public record PutCardsIntoLibraryEffect(CardSubject cards, boolean onTop, boolean randomOrder, boolean shuffled,
                                        PlayerSubject alwaysShuffled) implements CardEffect {

    public PutCardsIntoLibraryEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards instanceof CardSubject.InLibrary) {
            throw new IllegalArgumentException("The cards are put there from another zone: " + cards);
        }
        if (alwaysShuffled != null && (!shuffled || alwaysShuffled.declaresTarget())) {
            throw new IllegalArgumentException("Only a shuffle shuffles a library, of no target player: "
                    + alwaysShuffled);
        }
    }

    /** Put in a position: on top or on the bottom, in any or a random order. */
    public PutCardsIntoLibraryEffect(CardSubject cards, boolean onTop, boolean randomOrder) {
        this(cards, onTop, randomOrder, false, null);
    }

    /** "On the bottom of its owner's library", "in any order". */
    public static PutCardsIntoLibraryEffect onBottom(CardSubject cards) {
        return new PutCardsIntoLibraryEffect(cards, false, false);
    }

    /** "On the bottom of their library in a random order". */
    public static PutCardsIntoLibraryEffect onBottomInRandomOrder(CardSubject cards) {
        return new PutCardsIntoLibraryEffect(cards, false, true);
    }

    /** "On top of its owner's library", "in any order". */
    public static PutCardsIntoLibraryEffect onTop(CardSubject cards) {
        return new PutCardsIntoLibraryEffect(cards, true, false);
    }

    /** "Their owners shuffle those cards into their libraries": only libraries a card went into are shuffled. */
    public static PutCardsIntoLibraryEffect shuffledIn(CardSubject cards) {
        return new PutCardsIntoLibraryEffect(cards, false, false, true, null);
    }

    /**
     * "Shuffle it into its owner's library", "you shuffle up to four cards from your graveyard into your library": also
     * the library of each player {@code players} names, when none of the cards went into it (CR 701.24c, 701.24d).
     */
    public PutCardsIntoLibraryEffect alwaysShuffling(PlayerSubject players) {
        return new PutCardsIntoLibraryEffect(cards, false, false, true, Objects.requireNonNull(players, "players"));
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }

    @Override
    public boolean targetsAllExiledCardsInAbility() {
        return cards instanceof CardSubject.TargetExiledCard target && !target.exiledWithSource();
    }
}
