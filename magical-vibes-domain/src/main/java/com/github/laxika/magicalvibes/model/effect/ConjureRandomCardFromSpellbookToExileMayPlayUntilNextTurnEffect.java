package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Conjures one random spellbook card into exile with play permission until the next turn ends. */
public record ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect(
        List<CardPrintingReference> spellbook
) implements CardEffect {

    public ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("spellbook must not be empty");
        }
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
