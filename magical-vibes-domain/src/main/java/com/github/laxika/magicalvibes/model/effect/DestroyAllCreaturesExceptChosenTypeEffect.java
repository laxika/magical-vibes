package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts the controller to choose a creature type, then destroys every creature that is not of
 * that type.
 */
public record DestroyAllCreaturesExceptChosenTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
