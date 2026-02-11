package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

public record ActivateAbilityRequest(int permanentIndex, Integer xValue, UUID targetPermanentId) {
}
