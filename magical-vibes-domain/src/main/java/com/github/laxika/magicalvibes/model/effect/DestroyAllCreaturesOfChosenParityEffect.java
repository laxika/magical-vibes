package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys every creature whose mana value has the odd/even quality chosen earlier during the
 * current spell resolution.
 */
public record DestroyAllCreaturesOfChosenParityEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
