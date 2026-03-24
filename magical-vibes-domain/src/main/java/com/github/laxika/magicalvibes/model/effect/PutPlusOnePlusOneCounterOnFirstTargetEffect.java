package com.github.laxika.magicalvibes.model.effect;

/**
 * Unconditionally put one or more +1/+1 counters on the first targeted permanent
 * in a multi-target spell.
 * <p>
 * Designed for spells like Savage Stomp that always place a counter on the first
 * target before performing another action (e.g. fight) with both targets.
 *
 * @param count number of +1/+1 counters to place
 */
public record PutPlusOnePlusOneCounterOnFirstTargetEffect(
        int count
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
