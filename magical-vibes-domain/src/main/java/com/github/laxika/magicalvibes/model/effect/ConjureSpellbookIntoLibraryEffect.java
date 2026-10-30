package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Conjures the listed spellbook printings into the resolving controller's library. A printing that
 * is not implemented yet is skipped.
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
