package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.UUID;

/** Trigger descriptor for casting a spell or playing a land from a zone other than hand. */
public record PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects,
                                               UUID sourcePermanentId,
                                               UUID targetPlayerId)
        implements TemporaryGlobalTriggerEffect {

    public PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects) {
        this(resolvedEffects, null, null);
    }

    @Override
    public boolean matches(Zone sourceZone, UUID exiledSourcePermanentId) {
        return sourceZone == Zone.EXILE
                && sourcePermanentId != null
                && sourcePermanentId.equals(exiledSourcePermanentId);
    }

    @Override
    public TemporaryGlobalTriggerEffect bindTo(UUID sourcePermanentId, UUID targetPlayerId) {
        return new PlayFromOutsideHandTriggerEffect(resolvedEffects, sourcePermanentId, targetPlayerId);
    }
}
