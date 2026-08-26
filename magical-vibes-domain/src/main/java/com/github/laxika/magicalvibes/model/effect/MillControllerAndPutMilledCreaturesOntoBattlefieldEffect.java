package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then puts up to {@code maxCount} creature cards
 * milled by this resolution onto the battlefield.
 */
public record MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(int count, int maxCount)
        implements CardEffect {

    public MillControllerAndPutMilledCreaturesOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount cannot be negative");
        }
    }
}
