package com.github.laxika.magicalvibes.model.effect;

/**
 * {@link DestroyAllPermanentsEffect} rider: the controller of each permanent actually destroyed
 * by the preceding effect may search for a basic land card and put it onto the battlefield.
 * Duplicate controller ids represent separate searches for separate destroyed permanents.
 */
public record EachDestroyedPermanentControllerMaySearchBasicLandToBattlefieldEffect()
        implements CardEffect {
}
