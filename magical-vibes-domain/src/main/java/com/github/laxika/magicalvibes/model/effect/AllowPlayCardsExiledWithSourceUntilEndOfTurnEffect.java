package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until end of turn, you may play cards exiled with Urianger Augurelt. Spells you cast this way cost {2} less to
 * cast": the controller may play every card exiled with the source this turn, also the ones exiled with it later that
 * turn (CR 611.2c, through the source's {@code ExilePlayGrantBySource}), nonland spells cast this way costing
 * {@code spellCostReduction} less. Without a reduction write
 * {@code AllowPlayCardsFromExileEffect.thisTurn(exiledWithSource()).alsoCardsExiledLaterThisTurn()}.
 */
public record AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect(int spellCostReduction)
        implements CardEffect {

    public AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect {
        if (spellCostReduction < 0) {
            throw new IllegalArgumentException("Spell cost reduction cannot be negative");
        }
    }
}
