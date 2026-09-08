package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts the controller to choose a creature type, then gives all creatures that are not of that
 * type the specified power and toughness modifier until end of turn.
 */
public record BoostAllCreaturesExceptChosenTypeEffect(int powerBoost, int toughnessBoost)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
