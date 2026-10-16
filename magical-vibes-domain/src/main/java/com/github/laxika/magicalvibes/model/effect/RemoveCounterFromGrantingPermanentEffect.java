package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Remove counters from the permanent that granted this ability, at resolution, rather than from the
 * permanent activating it. Used for Aura-granted abilities whose removal is part of the granted text
 * rather than a cost, such as Heliod's Punishment's "Remove a task counter from Heliod's Punishment".
 * Removal is clamped at zero, so the ability can still be activated with no counters.
 *
 * <p>{@code grantingPermanentId} is null on the card definition and bound at activation time from
 * {@link com.github.laxika.magicalvibes.model.ActivatedAbility#getGrantSourcePermanentId()}, so the
 * counters are removed from the right permanent even if it is unattached in response.</p>
 */
public record RemoveCounterFromGrantingPermanentEffect(CounterType counterType, int count, UUID grantingPermanentId)
        implements CardEffect {

    public RemoveCounterFromGrantingPermanentEffect(CounterType counterType) {
        this(counterType, 1, null);
    }
}
