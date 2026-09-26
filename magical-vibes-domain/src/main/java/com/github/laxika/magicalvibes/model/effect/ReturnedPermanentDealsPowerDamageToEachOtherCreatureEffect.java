package com.github.laxika.magicalvibes.model.effect;

/**
 * The creature returned by a preceding targeted graveyard return deals damage equal to its power
 * to each other creature.
 */
public record ReturnedPermanentDealsPowerDamageToEachOtherCreatureEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
