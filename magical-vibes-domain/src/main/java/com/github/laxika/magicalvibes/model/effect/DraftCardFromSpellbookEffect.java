package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.List;
import java.util.Objects;

/** Offers three distinct cards from a digital spellbook and puts the chosen card into a destination. */
public record DraftCardFromSpellbookEffect(List<String> cardNames,
                                           DraftCardRecipient recipient,
                                           boolean revealChosenCard,
                                           boolean exileChosenCard,
                                           boolean putChosenCardOntoBattlefield,
                                           List<CardEffect> chosenCardEffects,
                                           LibrarySelectionFollowUp battlefieldSelectionFollowUp,
                                           boolean makeSelectedArtifactCreature) implements CardEffect {

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
                chosenCardEffects, null, false);
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames,
                                        LibrarySearchDestination destination,
                                        LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false,
                destination == LibrarySearchDestination.BATTLEFIELD, List.of(),
                battlefieldSelectionFollowUp, false);
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
                false, false, false, List.of(), null, true);
    }

    public DraftCardFromSpellbookEffect {
        Objects.requireNonNull(recipient, "recipient");
        if (exileChosenCard && putChosenCardOntoBattlefield) {
            throw new IllegalArgumentException("A drafted card cannot be exiled and put onto the battlefield");
        }
        cardNames = List.copyOf(cardNames);
        chosenCardEffects = List.copyOf(chosenCardEffects);
    }
}
