package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then puts one creature card milled this way onto the
 * battlefield. If more than one creature card was milled, the controller chooses one.
 */
public record MillControllerAndPutMilledCreatureOntoBattlefieldEffect(int count) implements CardEffect {
}
