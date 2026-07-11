package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Where a cost-modifying static effect lives: on a battlefield permanent (with that
 * permanent's controller), or on the spell being cast itself ({@link #SPELL_ITSELF},
 * where both fields are null).
 */
public record CostModificationSource(Permanent sourcePermanent, UUID controllerId) {

    public static final CostModificationSource SPELL_ITSELF = new CostModificationSource(null, null);

    public boolean controlledBy(UUID playerId) {
        return playerId != null && playerId.equals(controllerId);
    }
}
