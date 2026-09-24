package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Where a cost-modifying static effect lives: on a battlefield permanent, emblem, planar object,
 * or command-zone card (with its controller), or on the spell being cast itself ({@link #SPELL_ITSELF}).
 * Emblem, planar, and command-zone sources have a null {@code sourcePermanent}.
 */
public record CostModificationSource(Permanent sourcePermanent, UUID controllerId, UUID sourceCardId) {

    public static final CostModificationSource SPELL_ITSELF = new CostModificationSource(null, null, null);

    public CostModificationSource(Permanent sourcePermanent, UUID controllerId) {
        this(sourcePermanent, controllerId,
                sourcePermanent == null ? null : sourcePermanent.getCard().getId());
    }

    public boolean controlledBy(UUID playerId) {
        return playerId != null && playerId.equals(controllerId);
    }
}
