package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Conjures the listed spellbook cards into the resolving controller's library. */
public record ConjureSpellbookIntoLibraryEffect(
        List<String> cardNames,
        int copies
) implements CardEffect {

    public ConjureSpellbookIntoLibraryEffect {
        cardNames = List.copyOf(cardNames);
        if (copies < 0) {
            throw new IllegalArgumentException("copies cannot be negative");
        }
    }
}
