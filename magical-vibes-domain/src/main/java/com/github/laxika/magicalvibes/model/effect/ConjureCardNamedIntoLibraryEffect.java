package com.github.laxika.magicalvibes.model.effect;

/** Conjures the named card into the resolving controller's library. */
public record ConjureCardNamedIntoLibraryEffect(String cardName, int count) implements CardEffect {

    public ConjureCardNamedIntoLibraryEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
