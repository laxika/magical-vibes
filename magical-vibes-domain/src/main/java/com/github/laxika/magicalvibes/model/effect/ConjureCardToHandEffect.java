package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Conjures a full, non-token card by printing or by name into the controller's hand. */
public record ConjureCardToHandEffect(String setCode, String collectorNumber, String cardName,
                                     Set<Keyword> removedKeywords) implements CardEffect {

    public ConjureCardToHandEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, null, Set.of());
    }

    public ConjureCardToHandEffect(String cardName) {
        this(null, null, cardName, Set.of());
    }

    public ConjureCardToHandEffect(String cardName, Set<Keyword> removedKeywords) {
        this(null, null, cardName, removedKeywords);
    }

    public ConjureCardToHandEffect {
        removedKeywords = removedKeywords == null ? Set.of() : Set.copyOf(removedKeywords);
    }

    public static ConjureCardToHandEffect withoutKeyword(String cardName, Keyword keyword) {
        return new ConjureCardToHandEffect(null, null, cardName, Set.of(keyword));
    }
}
