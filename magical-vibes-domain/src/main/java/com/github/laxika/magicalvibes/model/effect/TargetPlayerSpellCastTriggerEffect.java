package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.UUID;

/** Temporary global trigger that fires when the bound player casts any spell. */
public record TargetPlayerSpellCastTriggerEffect(List<CardEffect> resolvedEffects,
                                                  UUID targetPlayerId)
        implements TemporaryGlobalTriggerEffect {

    public TargetPlayerSpellCastTriggerEffect(List<CardEffect> resolvedEffects) {
        this(resolvedEffects, null);
    }

    @Override
    public boolean matches(Zone sourceZone, UUID exiledSourcePermanentId) {
        return true;
    }

    @Override
    public boolean matchesCastingPlayer(UUID castingPlayerId) {
        return targetPlayerId != null && targetPlayerId.equals(castingPlayerId);
    }

    @Override
    public TemporaryGlobalTriggerEffect bindTo(UUID sourcePermanentId, UUID targetPlayerId) {
        return new TargetPlayerSpellCastTriggerEffect(resolvedEffects, targetPlayerId);
    }
}
