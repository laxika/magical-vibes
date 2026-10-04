package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import java.util.UUID;

public record RetetherAuraPlacement(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                    UUID attachmentTargetId, Permanent preparedPermanent,
                                    EnterWithCountersEffect additionalCounters) {

    public RetetherAuraPlacement(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                 UUID attachmentTargetId) {
        this(controllerId, graveyardOwnerId, auraCard, attachmentTargetId, null, null);
    }

    public RetetherAuraPlacement deepCopy() {
        return new RetetherAuraPlacement(controllerId, graveyardOwnerId, auraCard, attachmentTargetId,
                preparedPermanent == null ? null : new Permanent(preparedPermanent), additionalCounters);
    }
}
