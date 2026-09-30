package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Conjures one random spellbook card into exile and grants a free-cast permission until end of turn. */
public record ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect(
        List<CardPrintingReference> spellbook
) implements CardEffect {

    public ConjureRandomCardFromSpellbookToExileMayCastFreeUntilEndOfTurnEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("spellbook must not be empty");
        }
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
