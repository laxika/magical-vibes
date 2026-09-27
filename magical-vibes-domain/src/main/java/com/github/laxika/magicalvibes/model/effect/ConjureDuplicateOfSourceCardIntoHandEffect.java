package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Conjures a duplicate of the source card's printing into its controller's hand. */
public record ConjureDuplicateOfSourceCardIntoHandEffect(
        String fallbackSetCode,
        String fallbackCollectorNumber,
        Set<Keyword> removedKeywords
) implements CardEffect {

    public ConjureDuplicateOfSourceCardIntoHandEffect() {
        this(null, null, Set.of());
    }

    public ConjureDuplicateOfSourceCardIntoHandEffect(String fallbackSetCode, String fallbackCollectorNumber) {
        this(fallbackSetCode, fallbackCollectorNumber, Set.of());
    }

    public ConjureDuplicateOfSourceCardIntoHandEffect(Set<Keyword> removedKeywords) {
        this(null, null, removedKeywords);
    }

    public ConjureDuplicateOfSourceCardIntoHandEffect {
        removedKeywords = removedKeywords == null ? Set.of() : Set.copyOf(removedKeywords);
    }
}
