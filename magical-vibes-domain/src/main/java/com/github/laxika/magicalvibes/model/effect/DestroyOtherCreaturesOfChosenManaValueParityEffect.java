package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts for odd or even, then destroys each other creature whose mana value has the chosen
 * parity.
 */
public record DestroyOtherCreaturesOfChosenManaValueParityEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
