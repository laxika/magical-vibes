package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then puts each creature card milled this way onto
 * the battlefield with haste and schedules it to return to its owner's hand at the next end step.
 */
public record MillControllerAndTemporarilyReanimateMilledCreaturesEffect(int count)
        implements CardEffect {
}
