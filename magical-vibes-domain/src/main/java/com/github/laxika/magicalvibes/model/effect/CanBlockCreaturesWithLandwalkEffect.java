package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can block creatures with landwalk as though they didn't have
 * those abilities.
 */
public record CanBlockCreaturesWithLandwalkEffect() implements BlockabilityPermissionEffect {

    @Override
    public boolean blocksLandwalkAsThoughNoLandwalk() {
        return true;
    }
}
