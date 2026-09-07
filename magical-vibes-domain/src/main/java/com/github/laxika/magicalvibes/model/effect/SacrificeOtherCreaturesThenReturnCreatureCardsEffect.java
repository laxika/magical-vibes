package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player sacrifices all other creatures they control, then returns the creature cards that
 * were already in their graveyard to the battlefield. The pre-existing graveyard snapshot keeps
 * creatures sacrificed by the first step out of the return set.
 */
public record SacrificeOtherCreaturesThenReturnCreatureCardsEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
