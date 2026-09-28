package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Conjures one random listed printing into the resolving controller's hand. */
public record ConjureRandomCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook)
        implements CardEffect {

    public ConjureRandomCardFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("spellbook must not be empty");
        }
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
