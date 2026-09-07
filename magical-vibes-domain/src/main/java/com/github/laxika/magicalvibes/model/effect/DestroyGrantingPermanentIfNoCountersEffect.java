package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Destroys the permanent that granted the activated ability if it has no counters of the given
 * type. The granting permanent ID is bound when the ability is activated, so the effect still
 * refers to the same permanent if the Aura becomes unattached before resolution.
 */
public record DestroyGrantingPermanentIfNoCountersEffect(CounterType counterType,
                                                          UUID grantingPermanentId)
        implements CardEffect {

    public DestroyGrantingPermanentIfNoCountersEffect(CounterType counterType) {
        this(counterType, null);
    }
}
