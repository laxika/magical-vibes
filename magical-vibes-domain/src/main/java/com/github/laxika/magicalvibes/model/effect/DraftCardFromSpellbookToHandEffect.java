package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers three random spellbook cards and conjures the chosen card into hand. */
public record DraftCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook)
        implements CardEffect {

    public DraftCardFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.size() < 3) {
            throw new IllegalArgumentException("spellbook must contain at least three cards");
        }
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
