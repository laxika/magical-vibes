package com.github.laxika.magicalvibes.model;

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
 * @param grantedZoneChangeAbilities  continuously granted abilities that trigger on a zone change, its own ("when
 *                                    this dies") or another object's ("whenever a creature dies")
 */
public record DepartureSnapshot(Permanent permanent, UUID controllerId, boolean creature,
                                Map<Keyword, Integer> triggerKeywordInstances,
                                List<TriggeredAbility> grantedZoneChangeAbilities) {

    public DepartureSnapshot {
        Objects.requireNonNull(permanent, "permanent");
        triggerKeywordInstances = Map.copyOf(triggerKeywordInstances);
        grantedZoneChangeAbilities = List.copyOf(grantedZoneChangeAbilities);
    }

    public UUID permanentId() {
        return permanent.getId();
    }

    /** Copies the mutable last-known permanent; everything else is immutable. */
    public DepartureSnapshot deepCopy() {
        return new DepartureSnapshot(new Permanent(permanent), controllerId, creature, triggerKeywordInstances,
                grantedZoneChangeAbilities);
    }
}
