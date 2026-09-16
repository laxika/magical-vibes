package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers one nonland card from the targeted player's sideboard for a free cast by this effect's
 * controller.
 */
public record MayCastFromTargetPlayerSideboardWithoutPayingManaCostEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
