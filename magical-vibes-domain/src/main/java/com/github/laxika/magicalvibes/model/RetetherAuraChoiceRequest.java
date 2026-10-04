package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import java.util.List;
import java.util.UUID;

public record RetetherAuraChoiceRequest(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                        List<UUID> validTargetIds, Permanent preparedPermanent,
                                        EnterWithCountersEffect additionalCounters) {

    public RetetherAuraChoiceRequest(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                     List<UUID> validTargetIds) {
        this(controllerId, graveyardOwnerId, auraCard, validTargetIds, null, null);
    }

    public RetetherAuraChoiceRequest {
        validTargetIds = List.copyOf(validTargetIds);
    }

    public RetetherAuraChoiceRequest deepCopy() {
        return new RetetherAuraChoiceRequest(controllerId, graveyardOwnerId, auraCard, validTargetIds,
                preparedPermanent == null ? null : new Permanent(preparedPermanent), additionalCounters);
    }
}
