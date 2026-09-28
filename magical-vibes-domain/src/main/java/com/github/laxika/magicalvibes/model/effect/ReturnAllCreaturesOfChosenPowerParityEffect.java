package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns every creature whose effective power has the odd/even quality chosen earlier during
 * the current spell resolution.
 */
public record ReturnAllCreaturesOfChosenPowerParityEffect() implements RemovalEffect, BoardWipeEffect {

    @Override
    public RemovalKind removalKind() {
        return null;
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
