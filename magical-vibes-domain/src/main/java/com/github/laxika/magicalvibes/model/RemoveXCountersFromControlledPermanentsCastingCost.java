package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * An additional cost that removes X counters from among permanents controlled by the player
 * casting the spell. The chosen permanents may be repeated to remove multiple counters from one
 * permanent.
 */
public record RemoveXCountersFromControlledPermanentsCastingCost(
        CounterType counterType, PermanentPredicate permanentPredicate) implements CastingCost {
}
