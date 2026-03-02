package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts target creature card from an opponent's graveyard onto the battlefield under your control.
 * It gains haste. Exile it at the beginning of the next end step.
 * If that creature would leave the battlefield, exile it instead of putting it anywhere else.
 *
 * Used by: Gruesome Encore
 */
public record PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect() implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }
}
