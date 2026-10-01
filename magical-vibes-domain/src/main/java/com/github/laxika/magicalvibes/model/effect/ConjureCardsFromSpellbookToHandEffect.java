package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers an exact number of listed spellbook cards to conjure into hand. */
public record ConjureCardsFromSpellbookToHandEffect(
        List<ConjureCardFromSpellbookToHandEffect.CardPrintingReference> spellbook,
        int count,
        CardEffect chosenCardThenEffect) implements CardEffect {

    public ConjureCardsFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
        if (count < 1 || count > spellbook.size()) {
            throw new IllegalArgumentException("Spellbook choice count must be within the spellbook size");
        }
    }
}
