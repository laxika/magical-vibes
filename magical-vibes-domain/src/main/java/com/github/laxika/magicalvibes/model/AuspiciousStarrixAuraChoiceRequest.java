package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

public record AuspiciousStarrixAuraChoiceRequest(
        UUID controllerId, Card auraCard, List<UUID> validPermanentIds, List<UUID> validPlayerIds) {

    public AuspiciousStarrixAuraChoiceRequest {
        validPermanentIds = List.copyOf(validPermanentIds);
        validPlayerIds = List.copyOf(validPlayerIds);
    }
}
