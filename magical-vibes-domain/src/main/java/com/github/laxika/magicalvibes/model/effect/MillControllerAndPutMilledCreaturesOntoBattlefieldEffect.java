package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then puts each creature card milled by this
 * resolution onto the battlefield under the controller's control with haste. Each creature is
 * scheduled to return to its owner's hand at the beginning of the next end step.
 */
public record MillControllerAndPutMilledCreaturesOntoBattlefieldEffect(int count)
        implements CardEffect {
}
