package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

public record RetetherAuraChoiceRequest(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                        List<UUID> validTargetIds) {

    public RetetherAuraChoiceRequest {
        validTargetIds = List.copyOf(validTargetIds);
    }
}
