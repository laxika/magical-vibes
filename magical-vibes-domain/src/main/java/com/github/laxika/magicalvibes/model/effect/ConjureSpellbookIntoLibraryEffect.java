package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "Conjure [this]'s spellbook into your library [twice], then shuffle", "conjure the Power Nine into your library,
 * then shuffle": {@code copies} of each listed printing into the resolving controller's library, then that library is
 * shuffled. A printing that is not implemented yet is skipped.
 */
public record ConjureSpellbookIntoLibraryEffect(
        List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
        int copies
) implements CardEffect {

    public ConjureSpellbookIntoLibraryEffect {
        spellbook = List.copyOf(spellbook);
        if (copies < 0) {
            throw new IllegalArgumentException("copies cannot be negative");
        }
    }
}
