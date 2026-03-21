package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                       UUID targetId, Zone targetZone,
                                       String discardCostLabel) {
}
