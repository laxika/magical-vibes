package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then lets them put any number of the milled creature
 * cards into their hand.
 */
public record MillControllerAndPutAnyNumberOfMilledCreaturesIntoHandEffect(int count)
        implements CardEffect {
}
