package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys every creature with power greater than the power of the targeted creature.
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
