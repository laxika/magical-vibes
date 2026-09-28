package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Has the controller choose one matching permanent they control to exile and track with the
 * source permanent until the source leaves the battlefield.
 *
 * @param filter permanents that may be chosen
 * @param thenEffect optional reflexive ability queued after a successful exile
 */
public record ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter,
                                                                CardEffect thenEffect)
        implements CardEffect {

    public ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter) {
        this(filter, null);
    }
}
