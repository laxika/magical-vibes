package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/** Reduces Aura spells cast by this source's controller when they target its attachment. */
public record ReduceAuraCastCostForTargetingAttachedPermanentEffect(int amount)
        implements BattlefieldTargetCastCostReducingEffect {

    @Override
    public boolean appliesTo(UUID castingPlayerId, UUID sourceControllerId,
                             Permanent sourcePermanent, Card spell, List<UUID> targetIds) {
        return sourcePermanent != null
                && sourcePermanent.getAttachedTo() != null
                && sourceControllerId != null
                && sourceControllerId.equals(castingPlayerId)
                && spell != null
                && spell.isAura()
                && targetIds != null
                && targetIds.contains(sourcePermanent.getAttachedTo());
    }
}
