package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Unconditionally put one or more +1/+1 counters on the first targeted permanent
 * in a multi-target spell.
 * <p>
 * Designed for spells like Savage Stomp that always place a counter on the first
 * target before performing another action (e.g. fight) with both targets.
 * <p>
 * When {@code requiredSupertype} is non-null, the counters are only placed if the first target
 * has that supertype (e.g. Ancient Animus — only if the first target is {@link CardSupertype#LEGENDARY}).
 *
 * @param count             number of +1/+1 counters to place
 * @param requiredSupertype supertype the first target must have, or {@code null} for unconditional
 */
public record PutPlusOnePlusOneCounterOnFirstTargetEffect(
        int count,
        CardSupertype requiredSupertype
) implements CardEffect {

    public PutPlusOnePlusOneCounterOnFirstTargetEffect(int count) {
        this(count, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
