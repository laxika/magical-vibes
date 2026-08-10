package com.github.laxika.magicalvibes.model.effect;

/**
 * The active player reveals cards from the top of their library until a creature card is revealed.
 * That card is put onto the active player's battlefield, and all other revealed cards are put into
 * the active player's graveyard. The player target is retained for a separate upkeep target
 * restriction.
 */
public record ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
