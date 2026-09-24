package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger-only effect for a Vehicle that perpetually gets a power/toughness boost when a matching
 * creature crews it.
 */
public record PerpetuallyBoostVehicleWhenMatchingCreatureCrewsEffect(
        PermanentPredicate crewingCreaturePredicate,
        int powerBoost,
        int toughnessBoost
) implements CardEffect {
}
