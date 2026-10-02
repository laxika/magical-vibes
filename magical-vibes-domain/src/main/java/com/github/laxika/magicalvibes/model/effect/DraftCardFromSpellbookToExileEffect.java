package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers three distinct spellbook cards and exiles the chosen card with the source permanent. */
public record DraftCardFromSpellbookToExileEffect(List<String> cardNames, boolean faceDown)
        implements CardEffect {

    public DraftCardFromSpellbookToExileEffect(List<String> cardNames) {
        this(cardNames, true);
    }

    public DraftCardFromSpellbookToExileEffect {
        cardNames = List.copyOf(cardNames);
    }
}
