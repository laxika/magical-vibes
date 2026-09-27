package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers the listed printings as a spellbook choice and conjures the chosen card into hand. */
public record ConjureCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook)
        implements CardEffect {

    public ConjureCardFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
