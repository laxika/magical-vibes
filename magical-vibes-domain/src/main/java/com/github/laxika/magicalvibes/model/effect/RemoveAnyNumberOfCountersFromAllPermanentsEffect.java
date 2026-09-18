package com.github.laxika.magicalvibes.model.effect;

/**
 * During resolution, the controller may remove any number of concrete counters from permanents
 * on all battlefields. The number actually removed is recorded on the resolving stack entry.
 */
public record RemoveAnyNumberOfCountersFromAllPermanentsEffect() implements CardEffect {
}
