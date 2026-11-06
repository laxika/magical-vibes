package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Create a token that's a copy of it at the beginning of the next end step. The token enters with
 * [N] +1/+1 counters on it" (Ochre Jelly): registers the delayed token copy of the source as the death trigger
 * resolves. The counter amount is evaluated then, against the stack entry, so "half that many" reads the triggering
 * permanent's counters as it last existed on the battlefield (CR 603.10a); "if it had two or more" is the trigger's
 * filter.
 *
 * @param plusOnePlusOneCounters the number of +1/+1 counters the token enters with
 */
public record RegisterDelayedCreateTokenCopyOfDyingSourceEffect(DynamicAmount plusOnePlusOneCounters)
        implements CardEffect {
}
