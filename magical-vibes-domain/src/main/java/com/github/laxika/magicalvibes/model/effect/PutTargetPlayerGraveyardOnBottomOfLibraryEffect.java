package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts every card from the target player's graveyard on the bottom of their library in a random
 * order.
 */
public record PutTargetPlayerGraveyardOnBottomOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
