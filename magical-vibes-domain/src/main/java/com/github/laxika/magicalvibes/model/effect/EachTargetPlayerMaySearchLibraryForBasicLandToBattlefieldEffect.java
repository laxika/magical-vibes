package com.github.laxika.magicalvibes.model.effect;

/**
 * Each targeted player may search their library for a basic land card, put it onto the battlefield
 * under their control, then shuffle.
 */
public record EachTargetPlayerMaySearchLibraryForBasicLandToBattlefieldEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
