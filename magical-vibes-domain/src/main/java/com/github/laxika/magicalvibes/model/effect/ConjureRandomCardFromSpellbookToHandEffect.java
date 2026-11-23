package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * Conjures a dynamic number of random listed printings into the resolving controller's hand. The hand is hidden
 * (CR 400.2): the cards are shown to every player only where the text says "and reveal it" ({@link #revealed()}).
 *
 * @param spellbook the printings picked from at random
 * @param count     how many are conjured
 * @param reveal    whether the conjured cards are revealed
 */
public record ConjureRandomCardFromSpellbookToHandEffect(
        List<CardPrintingReference> spellbook, DynamicAmount count, boolean reveal)
        implements CardEffect {

    public ConjureRandomCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook) {
        this(spellbook, new Fixed(1));
    }

    public ConjureRandomCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook, DynamicAmount count) {
        this(spellbook, count, false);
    }

    /** "Conjure ... at random into your hand and reveal it". */
    public ConjureRandomCardFromSpellbookToHandEffect revealed() {
        return new ConjureRandomCardFromSpellbookToHandEffect(spellbook, count, true);
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
