package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/** Revealed cards returned to a library after the corresponding battlefield-entry choices finish. */
public record BattlefieldEntryLibraryRemainder(UUID playerId, List<Card> cards, boolean shuffleLibrary) {
    public BattlefieldEntryLibraryRemainder {
        cards = List.copyOf(cards);
    }
}
