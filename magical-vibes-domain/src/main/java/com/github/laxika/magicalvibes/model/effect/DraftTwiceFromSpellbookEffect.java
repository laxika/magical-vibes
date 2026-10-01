package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Drafts two cards from a spellbook, then lets the controller put one onto the battlefield tapped. */
public record DraftTwiceFromSpellbookEffect(
        List<DraftFromSpellbookEffect.SpellbookCard> spellbook) implements CardEffect {

    public DraftTwiceFromSpellbookEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.size() < 3) {
            throw new IllegalArgumentException("A spellbook must contain at least three cards");
        }
    }
}
