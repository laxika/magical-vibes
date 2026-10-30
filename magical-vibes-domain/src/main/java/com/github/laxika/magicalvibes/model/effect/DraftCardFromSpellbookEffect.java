package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.List;
import java.util.Objects;

/**
 * Offers three distinct cards from a digital spellbook and puts the chosen card into a destination.
 *
 * <p>The spellbook lists printings rather than names so resolution creates exactly those cards; a
 * printing that is not implemented yet is left out of the offer.
 *
 * <p>With {@code conjure} set ("conjure a card of your choice from … spellbook") the whole spellbook
 * is offered instead of three random cards, and the chosen card counts as conjured, so
 * "whenever you conjure" abilities trigger. Plain drafts are not conjures.
 */
public record DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                           DraftCardRecipient recipient,
                                           boolean revealChosenCard,
                                           boolean exileChosenCard,
                                           boolean putChosenCardOntoBattlefield,
                                           List<CardEffect> chosenCardEffects,
                                           LibrarySelectionFollowUp battlefieldSelectionFollowUp,
                                           boolean makeSelectedArtifactCreature,
                                           boolean conjure) implements CardEffect {

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        this(spellbook, DraftCardRecipient.CONTROLLER, false, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        boolean exileChosenCard) {
        this(spellbook, DraftCardRecipient.CONTROLLER, false, exileChosenCard, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        boolean exileChosenCard, boolean putChosenCardOntoBattlefield) {
        this(spellbook, DraftCardRecipient.CONTROLLER, false, exileChosenCard,
                putChosenCardOntoBattlefield, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        List<CardEffect> chosenCardEffects) {
        this(spellbook, DraftCardRecipient.CONTROLLER, false, false, false, chosenCardEffects);
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        DraftCardRecipient recipient, boolean revealChosenCard) {
        this(spellbook, recipient, revealChosenCard, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        DraftCardRecipient recipient,
                                        boolean revealChosenCard, boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield,
                                        List<CardEffect> chosenCardEffects) {
        this(spellbook, recipient, revealChosenCard, exileChosenCard, putChosenCardOntoBattlefield,
                chosenCardEffects, null, false, false);
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        LibrarySearchDestination destination,
                                        LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        this(spellbook, DraftCardRecipient.CONTROLLER, false, false,
                destination == LibrarySearchDestination.BATTLEFIELD, List.of(),
                battlefieldSelectionFollowUp, false, false);
        if (destination != LibrarySearchDestination.HAND
                && destination != LibrarySearchDestination.BATTLEFIELD) {
            throw new IllegalArgumentException("Spellbook drafts support only hand or battlefield destinations");
        }
    }

    public static DraftCardFromSpellbookEffect toBattlefield(
            List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
            LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        return new DraftCardFromSpellbookEffect(
                spellbook, LibrarySearchDestination.BATTLEFIELD, battlefieldSelectionFollowUp);
    }

    public static DraftCardFromSpellbookEffect withArtifactCreatureGrant(
            List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        return new DraftCardFromSpellbookEffect(spellbook, DraftCardRecipient.CONTROLLER,
                false, false, false, List.of(), null, true, false);
    }

    /** "Conjure a card of your choice from … spellbook onto the battlefield." */
    public static DraftCardFromSpellbookEffect conjureToBattlefield(
            List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        return new DraftCardFromSpellbookEffect(spellbook, DraftCardRecipient.CONTROLLER,
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
        spellbook = List.copyOf(spellbook);
        chosenCardEffects = List.copyOf(chosenCardEffects);
    }
}
