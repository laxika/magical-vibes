package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.UUID;

/** Trigger descriptor for casting a spell or playing a land from a zone other than hand. */
public record PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects,
                                               UUID sourcePermanentId,
                                               UUID targetPlayerId,
                                               TargetFilter targetFilter)
        implements TemporaryGlobalTriggerEffect {

    public PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects) {
        this(resolvedEffects, null, null, null);
    }

    /** Targeted form for triggers such as "whenever you cast a spell from outside your hand, ...". */
    public PlayFromOutsideHandTriggerEffect(List<CardEffect> resolvedEffects, TargetFilter targetFilter) {
        this(resolvedEffects, null, null, targetFilter);
    }

    @Override
    public boolean matches(Zone sourceZone, UUID exiledSourcePermanentId) {
        return sourceZone == Zone.EXILE
                && sourcePermanentId != null
                && sourcePermanentId.equals(exiledSourcePermanentId);
    }

    @Override
    public TemporaryGlobalTriggerEffect bindTo(UUID sourcePermanentId, UUID targetPlayerId) {
        return new PlayFromOutsideHandTriggerEffect(resolvedEffects, sourcePermanentId, targetPlayerId,
                targetFilter);
    }
}
