package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Where a cost-modifying static effect lives: on a battlefield permanent, emblem, planar object,
 * or command-zone card (with its controller), or on the spell being cast itself ({@link #SPELL_ITSELF}).
 * Emblem, planar, and command-zone sources have a null {@code sourcePermanent}.
 */
public record CostModificationSource(Permanent sourcePermanent, UUID controllerId, Card sourceCard) {

    public CostModificationSource(Permanent sourcePermanent, UUID controllerId) {
        this(sourcePermanent, controllerId, sourcePermanent == null ? null : sourcePermanent.getCard());
    }

    public static final CostModificationSource SPELL_ITSELF = new CostModificationSource(null, null, null);

    public UUID sourceCardId() {
        return sourceCard == null ? null : sourceCard.getId();
    }

    public boolean controlledBy(UUID playerId) {
        return playerId != null && playerId.equals(controllerId);
    }
}
