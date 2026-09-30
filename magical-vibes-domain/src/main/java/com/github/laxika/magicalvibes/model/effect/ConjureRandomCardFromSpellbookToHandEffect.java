package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Conjures a dynamic number of random listed printings into the resolving controller's hand. */
public record ConjureRandomCardFromSpellbookToHandEffect(
        List<CardPrintingReference> spellbook, DynamicAmount count)
        implements CardEffect {

    public ConjureRandomCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook) {
        this(spellbook, new Fixed(1));
    }

    public ConjureRandomCardFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
        if (spellbook.isEmpty()) {
            throw new IllegalArgumentException("spellbook must not be empty");
        }
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
