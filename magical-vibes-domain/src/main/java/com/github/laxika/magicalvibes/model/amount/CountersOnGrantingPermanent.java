package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * The number of counters of the given type on the permanent that granted the activated ability
 * being resolved (the Aura/Equipment, not the activating creature). {@code grantingPermanentId}
 * is null on the card definition and bound at activation time from
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()} in
 * {@code ActivatedAbilityExecutionService.snapshotEffects}, so the count still resolves against
 * the right granting permanent (Archery Training's "{T}: … deals X damage … where X is the number
 * of arrow counters on Archery Training"). Evaluates to 0 while unbound.
 */
public record CountersOnGrantingPermanent(CounterType counterType, UUID grantingPermanentId)
        implements DynamicAmount {

    public CountersOnGrantingPermanent(CounterType counterType) {
        this(counterType, null);
    }
}
