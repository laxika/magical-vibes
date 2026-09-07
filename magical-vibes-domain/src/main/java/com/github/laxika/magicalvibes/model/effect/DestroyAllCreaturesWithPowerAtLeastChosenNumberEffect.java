package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a number from zero through ten during resolution, then destroys all
 * creatures whose power is at least that number.
 */
public record DestroyAllCreaturesWithPowerAtLeastChosenNumberEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
