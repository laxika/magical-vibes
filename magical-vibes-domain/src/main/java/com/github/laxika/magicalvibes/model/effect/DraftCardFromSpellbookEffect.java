package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.List;
import java.util.Objects;

/**
 * Offers three distinct cards from a digital spellbook and puts the chosen card into a destination.
 *
 * <p>With {@code conjure} set ("conjure a card of your choice from … spellbook") the whole spellbook
 * is offered instead of three random cards, and the chosen card counts as conjured, so
 * "whenever you conjure" abilities trigger. Plain drafts are not conjures.
 */
public record DraftCardFromSpellbookEffect(List<String> cardNames,
                                           DraftCardRecipient recipient,
                                           boolean revealChosenCard,
                                           boolean exileChosenCard,
                                           boolean putChosenCardOntoBattlefield,
                                           List<CardEffect> chosenCardEffects,
                                           LibrarySelectionFollowUp battlefieldSelectionFollowUp,
                                           boolean makeSelectedArtifactCreature,
                                           boolean conjure) implements CardEffect {

    public DraftCardFromSpellbookEffect(List<String> cardNames) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, boolean exileChosenCard) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, exileChosenCard, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, exileChosenCard,
                putChosenCardOntoBattlefield, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, List<CardEffect> chosenCardEffects) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false, false, chosenCardEffects);
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, DraftCardRecipient recipient,
                                        boolean revealChosenCard) {
        this(cardNames, recipient, revealChosenCard, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, DraftCardRecipient recipient,
                                        boolean revealChosenCard, boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield,
                                        List<CardEffect> chosenCardEffects) {
        this(cardNames, recipient, revealChosenCard, exileChosenCard, putChosenCardOntoBattlefield,
                chosenCardEffects, null, false, false);
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames,
                                        LibrarySearchDestination destination,
                                        LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false,
                destination == LibrarySearchDestination.BATTLEFIELD, List.of(),
                battlefieldSelectionFollowUp, false, false);
        if (destination != LibrarySearchDestination.HAND
                && destination != LibrarySearchDestination.BATTLEFIELD) {
            throw new IllegalArgumentException("Spellbook drafts support only hand or battlefield destinations");
        }
    }

    public static DraftCardFromSpellbookEffect toBattlefield(
            List<String> cardNames, LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        return new DraftCardFromSpellbookEffect(
                cardNames, LibrarySearchDestination.BATTLEFIELD, battlefieldSelectionFollowUp);
    }

    public static DraftCardFromSpellbookEffect withArtifactCreatureGrant(List<String> cardNames) {
        return new DraftCardFromSpellbookEffect(cardNames, DraftCardRecipient.CONTROLLER,
                false, false, false, List.of(), null, true, false);
    }

    /** "Conjure a card of your choice from … spellbook onto the battlefield." */
    public static DraftCardFromSpellbookEffect conjureToBattlefield(List<String> cardNames) {
        return new DraftCardFromSpellbookEffect(cardNames, DraftCardRecipient.CONTROLLER,
                false, false, true, List.of(), null, false, true);
    }

    public DraftCardFromSpellbookEffect {
        Objects.requireNonNull(recipient, "recipient");
        if (exileChosenCard && putChosenCardOntoBattlefield) {
            throw new IllegalArgumentException("A drafted card cannot be exiled and put onto the battlefield");
        }
        if (conjure && (battlefieldSelectionFollowUp != null || makeSelectedArtifactCreature)) {
            throw new IllegalArgumentException("Conjuring from a spellbook does not support selection follow-ups");
        }
        cardNames = List.copyOf(cardNames);
        chosenCardEffects = List.copyOf(chosenCardEffects);
    }
}
