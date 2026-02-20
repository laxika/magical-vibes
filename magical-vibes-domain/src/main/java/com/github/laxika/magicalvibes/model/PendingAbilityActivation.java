package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.UUID;

public record PendingAbilityActivation(UUID sourcePermanentId, int abilityIndex, int xValue,
                                       UUID targetPermanentId, Zone targetZone,
                                       CardType discardCostType) {
}
