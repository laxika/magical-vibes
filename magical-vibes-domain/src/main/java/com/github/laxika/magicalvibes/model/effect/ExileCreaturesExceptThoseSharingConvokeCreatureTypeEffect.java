package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every creature except creatures sharing a creature type with a creature that convoked the
 * resolving spell.
 */
public record ExileCreaturesExceptThoseSharingConvokeCreatureTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
