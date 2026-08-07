package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Put counters on the Equipment that granted this ability, not on the permanent activating it.
 * Used for Equipment-granted abilities like Hankyu's "{T}: Put an aim counter on Hankyu", where the
 * equipped creature activates the ability but the counters live on the Equipment.
 *
 * <p>{@code equipmentId} is null on the card definition and bound at activation time from
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()}, so the
 * counters still land on the right Equipment if it is unattached in response.</p>
 */
public record PutCountersOnGrantingEquipmentEffect(CounterType counterType, int count, UUID equipmentId)
        implements CardEffect {

    public PutCountersOnGrantingEquipmentEffect(CounterType counterType) {
        this(counterType, 1, null);
    }
}
