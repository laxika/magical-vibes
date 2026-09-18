package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts the controller to choose a creature type, then returns every creature that is not of
 * that type to its owner's hand.
 */
public record ReturnAllCreaturesExceptChosenTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
