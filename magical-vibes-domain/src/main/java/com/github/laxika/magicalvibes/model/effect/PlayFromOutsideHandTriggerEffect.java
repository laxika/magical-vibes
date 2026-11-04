package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.UUID;

/**
 * The effects of a temporary global "whenever you cast a spell or play a land from among the cards exiled with it this
 * turn" ability (Apple of Eden, Isu Relic), bound as it is registered to the permanent whose exiled cards it watches. A
 * printed "from anywhere other than your hand" ability is a trigger filter instead
 * ({@code Triggers.youCastSpell().notCastFrom(Zone.HAND)}, {@code Triggers.youPlayLand().notPlayedFrom(Zone.HAND)}).
 */
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
