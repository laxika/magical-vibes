package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Offers three distinct spellbook cards and exiles the chosen card with the source permanent. The
 * spellbook lists printings; a printing that is not implemented yet is left out of the offer.
 */
public record DraftCardFromSpellbookToExileEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook,
                                                  boolean faceDown)
        implements CardEffect {

    public DraftCardFromSpellbookToExileEffect(List<DraftFromSpellbookEffect.SpellbookCard> spellbook) {
        this(spellbook, true);
    }

    public DraftCardFromSpellbookToExileEffect {
        spellbook = List.copyOf(spellbook);
    }
}
