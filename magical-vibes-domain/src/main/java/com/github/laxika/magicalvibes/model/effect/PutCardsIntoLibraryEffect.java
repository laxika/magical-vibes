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
 * @param cards       the cards put into the library
 * @param onTop       on top; otherwise on the bottom
 * @param randomOrder "in a random order"
 */
public record PutCardsIntoLibraryEffect(CardSubject cards, boolean onTop, boolean randomOrder) implements CardEffect {

    public PutCardsIntoLibraryEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards instanceof CardSubject.InLibrary) {
            throw new IllegalArgumentException("The cards are put there from another zone: " + cards);
        }
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

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }

    @Override
    public boolean targetsAllExiledCardsInAbility() {
        return cards instanceof CardSubject.TargetExiledCard target && !target.exiledWithSource();
    }
}
