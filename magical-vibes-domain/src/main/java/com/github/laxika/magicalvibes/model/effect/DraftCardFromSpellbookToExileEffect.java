package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers three distinct spellbook cards and exiles the chosen card face down with the source permanent. */
public record DraftCardFromSpellbookToExileEffect(List<String> cardNames) implements CardEffect {

    public DraftCardFromSpellbookToExileEffect {
        cardNames = List.copyOf(cardNames);
    }
}
