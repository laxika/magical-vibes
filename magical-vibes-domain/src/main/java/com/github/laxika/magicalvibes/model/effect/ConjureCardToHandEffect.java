package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/** Conjures a named card into the controller's hand, optionally removing keywords from it. */
public record ConjureCardToHandEffect(String cardName, Set<Keyword> removedKeywords) implements CardEffect {

    public ConjureCardToHandEffect(String cardName) {
        this(cardName, Set.of());
    }

    public ConjureCardToHandEffect {
        removedKeywords = removedKeywords == null ? Set.of() : Set.copyOf(removedKeywords);
    }

    public static ConjureCardToHandEffect withoutKeyword(String cardName, Keyword keyword) {
        return new ConjureCardToHandEffect(cardName, Set.of(keyword));
    }
}
