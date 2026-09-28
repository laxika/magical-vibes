package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller permission to play every card currently exiled with the source permanent
 * until end of turn. Nonland spells played through the permission may receive a generic cost
 * reduction.
 */
public record AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect(int spellCostReduction)
        implements CardEffect {

    public AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect {
        if (spellCostReduction < 0) {
            throw new IllegalArgumentException("Spell cost reduction cannot be negative");
        }
    }
}
