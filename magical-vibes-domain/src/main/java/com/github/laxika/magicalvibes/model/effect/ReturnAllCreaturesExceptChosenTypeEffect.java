package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a creature type at resolution, then returns every creature that is not
 * of that type to its owner's hand.
 */
public record ReturnAllCreaturesExceptChosenTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
