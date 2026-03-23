package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
}
