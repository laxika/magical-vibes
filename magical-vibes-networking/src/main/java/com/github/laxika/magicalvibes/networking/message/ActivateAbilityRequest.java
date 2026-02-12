package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.TargetZone;

import java.util.UUID;

public record ActivateAbilityRequest(int permanentIndex, Integer xValue, UUID targetPermanentId, TargetZone targetZone) {
}
