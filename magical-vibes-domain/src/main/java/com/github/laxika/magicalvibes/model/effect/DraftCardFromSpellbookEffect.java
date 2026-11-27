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
 *
 * <p>{@code player} is who is offered the cards, chooses one and owns it (the first player the subject names); it
 * declares no target ({@code targetPlayerDeclaredElsewhere()} reads a player another effect declared).
 *
 * <p>The step pauses at its end on the choice and records {@code StepKind.CONJURE} as it is answered: the drafted card
 * in the zone it went to (a hand, or exile), or the permanent it became, so "that card perpetually gains ..." and "put
 * those cards onto the battlefield" are later steps over {@code Subjects.cardsThisWay(StepKind.CONJURE)}. The cards
 * offered and not drafted were never in the game and go nowhere. {@code battlefieldSelectionFollowUp} is what a draft
 * onto the battlefield does next with the permanent ("when an Equipment enters this way, attach it ..."). "Draft a card
 * from [its] spellbook twice" is {@link #times}: one draft after another, each from three new random cards, recorded
 * together as one result. A drafted card going into a hand is named only to its drafter (CR 400.2).
 *
 * @param drafts how many drafts the step makes, one after another
 */
public record DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                           PlayerSubject player,
                                           boolean revealChosenCard,
                                           boolean exileChosenCard,
                                           boolean putChosenCardOntoBattlefield,
                                           List<CardEffect> chosenCardEffects,
                                           LibrarySelectionFollowUp battlefieldSelectionFollowUp,
                                           boolean conjure,
                                           int drafts) implements CardEffect {

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        PlayerSubject player,
                                        boolean revealChosenCard,
                                        boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield,
                                        List<CardEffect> chosenCardEffects,
                                        LibrarySelectionFollowUp battlefieldSelectionFollowUp,
                                        boolean conjure) {
        this(spellbook, player, revealChosenCard, exileChosenCard, putChosenCardOntoBattlefield, chosenCardEffects,
                battlefieldSelectionFollowUp, conjure, 1);
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        this(spellbook, Subjects.you(), false, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        boolean exileChosenCard) {
        this(spellbook, Subjects.you(), false, exileChosenCard, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        boolean exileChosenCard, boolean putChosenCardOntoBattlefield) {
        this(spellbook, Subjects.you(), false, exileChosenCard,
                putChosenCardOntoBattlefield, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        List<CardEffect> chosenCardEffects) {
        this(spellbook, Subjects.you(), false, false, false, chosenCardEffects);
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        PlayerSubject player, boolean revealChosenCard) {
        this(spellbook, player, revealChosenCard, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        PlayerSubject player,
                                        boolean revealChosenCard, boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield,
                                        List<CardEffect> chosenCardEffects) {
        this(spellbook, player, revealChosenCard, exileChosenCard, putChosenCardOntoBattlefield,
                chosenCardEffects, null, false);
    }

    public DraftCardFromSpellbookEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                        LibrarySearchDestination destination,
                                        LibrarySelectionFollowUp battlefieldSelectionFollowUp) {
        this(spellbook, Subjects.you(), false, false,
                destination == LibrarySearchDestination.BATTLEFIELD, List.of(),
                battlefieldSelectionFollowUp, false);
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

    /** "Draft a card from [this]'s spellbook {@code drafts} times": one draft after another, recorded together. */
    public DraftCardFromSpellbookEffect times(int drafts) {
        return new DraftCardFromSpellbookEffect(spellbook, player, revealChosenCard, exileChosenCard,
                putChosenCardOntoBattlefield, chosenCardEffects, battlefieldSelectionFollowUp, conjure, drafts);
    }

    /** "Conjure a card of your choice from … spellbook onto the battlefield." */
    public static DraftCardFromSpellbookEffect conjureToBattlefield(
            List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        return new DraftCardFromSpellbookEffect(spellbook, Subjects.you(),
                false, false, true, List.of(), null, true);
    }

    public DraftCardFromSpellbookEffect {
        Objects.requireNonNull(player, "player");
        if (exileChosenCard && putChosenCardOntoBattlefield) {
            throw new IllegalArgumentException("A drafted card cannot be exiled and put onto the battlefield");
        }
        if (conjure && battlefieldSelectionFollowUp != null) {
            throw new IllegalArgumentException("Conjuring from a spellbook does not support selection follow-ups");
        }
        if (drafts < 1) {
            throw new IllegalArgumentException("A draft step drafts at least once: " + drafts);
        }
        if (drafts > 1 && (exileChosenCard || putChosenCardOntoBattlefield || battlefieldSelectionFollowUp != null
                || !chosenCardEffects.isEmpty())) {
            throw new IllegalArgumentException("Only drafts into a hand are repeated");
        }
        spellbook = List.copyOf(spellbook);
        chosenCardEffects = List.copyOf(chosenCardEffects);
    }
}
