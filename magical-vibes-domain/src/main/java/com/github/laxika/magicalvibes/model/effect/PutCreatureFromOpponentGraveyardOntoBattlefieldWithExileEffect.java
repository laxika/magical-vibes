package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts target creature card from an opponent's graveyard onto the battlefield under your control.
 * It gains haste. Exile it at the beginning of the next end step.
 * If that creature would leave the battlefield, exile it instead of putting it anywhere else.
 *
 * Works as a SPELL (target chosen at cast time) or as an ON_ENTER_BATTLEFIELD trigger
 * (target chosen at trigger time via a multi-graveyard choice over opponents' creature cards).
 *
 * Used by: Gruesome Encore (SPELL), Puppeteer Clique (ETB)
 */
public record PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect() implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }
}
