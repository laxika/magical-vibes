package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

public record ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone) {
}
