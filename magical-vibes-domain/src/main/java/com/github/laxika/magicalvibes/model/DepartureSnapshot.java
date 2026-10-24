package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * What one departing permanent looked like immediately before its {@link DepartureBatch} was performed,
 * so leaves-the-battlefield abilities of every object in the same event look back in time (CR 603.10a).
 *
 * @param permanent                   last-known copy, with its effective card and last-known power and toughness
 * @param controllerId                its controller immediately before it left
 * @param creature                    whether it was a creature immediately before it left
 * @param triggerKeywordInstances     its persist and undying instance counts, for creatures
 * @param grantedCreatureDeathEffects continuously granted "whenever a creature dies" abilities, for creatures
 * @param grantedSelfDeathEffects     continuously granted "when this dies" abilities
 */
public record DepartureSnapshot(Permanent permanent, UUID controllerId, boolean creature,
                                Map<Keyword, Integer> triggerKeywordInstances,
                                List<CardEffect> grantedCreatureDeathEffects,
                                List<CardEffect> grantedSelfDeathEffects) {

    public DepartureSnapshot {
        Objects.requireNonNull(permanent, "permanent");
        triggerKeywordInstances = Map.copyOf(triggerKeywordInstances);
        grantedCreatureDeathEffects = List.copyOf(grantedCreatureDeathEffects);
        grantedSelfDeathEffects = List.copyOf(grantedSelfDeathEffects);
    }

    public UUID permanentId() {
        return permanent.getId();
    }

    /** Copies the mutable last-known permanent; everything else is immutable. */
    public DepartureSnapshot deepCopy() {
        return new DepartureSnapshot(new Permanent(permanent), controllerId, creature, triggerKeywordInstances,
                grantedCreatureDeathEffects, grantedSelfDeathEffects);
    }
}
