package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Has the controller choose one matching permanent they control to exile and track with the
 * source permanent until the source leaves the battlefield.
 *
 * @param filter permanents that may be chosen
 * @param thenEffect optional reflexive ability queued after a successful exile
 * @param untilSourceLeaves whether the exiled card returns immediately when the source leaves the
 *                          battlefield (Yannik, Scavenging Sentinel: "until Yannik leaves the
 *                          battlefield"), rather than through a leaves-the-battlefield ability of the
 *                          source (Wormfang Newt)
 */
public record ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter,
                                                                CardEffect thenEffect,
                                                                boolean untilSourceLeaves)
        implements CardEffect {

    public ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter) {
        this(filter, null, false);
    }

    public ExilePermanentYouControlAndTrackWithSourceEffect(PermanentPredicate filter, CardEffect thenEffect) {
        this(filter, thenEffect, false);
    }
}
