package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.List;
import java.util.Objects;

/** Drafts one card from a fixed, digital-only spellbook into a specified destination. */
public record DraftCardFromSpellbookEffect(List<String> cardNames,
                                            LibrarySearchDestination destination,
                                            LibrarySelectionFollowUp battlefieldSelectionFollowUp)
        implements CardEffect {

    public DraftCardFromSpellbookEffect(List<String> cardNames) {
        this(cardNames, LibrarySearchDestination.HAND, null);
    }

    public DraftCardFromSpellbookEffect {
        cardNames = List.copyOf(cardNames);
        Objects.requireNonNull(destination, "destination");
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
}
