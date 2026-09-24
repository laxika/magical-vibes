package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys all creatures with power greater than the targeted creature's power.
 */
public record DestroyAllCreaturesWithPowerGreaterThanTargetEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
