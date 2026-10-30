package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Conjures a full, non-token card of the given printing into the controller's hand. */
public record ConjureCardToHandEffect(String setCode, String collectorNumber,
                                     Set<Keyword> removedKeywords) implements CardEffect {

    public ConjureCardToHandEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, Set.of());
    }

    public ConjureCardToHandEffect {
        removedKeywords = removedKeywords == null ? Set.of() : Set.copyOf(removedKeywords);
    }

    public static ConjureCardToHandEffect withoutKeyword(String setCode, String collectorNumber,
                                                         Keyword keyword) {
        return new ConjureCardToHandEffect(setCode, collectorNumber, Set.of(keyword));
    }
}
