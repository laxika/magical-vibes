package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A battlefield activated ability suspended while its controller chooses how many source
 * counters to remove as a cost. The chosen amount is passed back as the ability's X value.
 */
public record PendingAbilityCounterCostActivation(UUID sourcePermanentId, int abilityIndex,
                                                   UUID targetId, Zone targetZone,
                                                   CounterType counterType) {
}
