package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player searches their library for a basic land card, puts it onto the battlefield
 * tapped, then shuffles.
 *
 * <p>Targets a player ({@link #canTargetPlayer()} returns true). The search is mandatory but the
 * player may fail to find. Used by Fertilid.
 */
public record TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
