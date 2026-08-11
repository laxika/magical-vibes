package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player reveals their hand and discards every creature card of a chosen type, then
 * every creature of that type the player controls is destroyed without regeneration.
 */
public record TargetPlayerDiscardsAndDestroysCreaturesOfChosenTypeEffect() implements BoardWipeEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
