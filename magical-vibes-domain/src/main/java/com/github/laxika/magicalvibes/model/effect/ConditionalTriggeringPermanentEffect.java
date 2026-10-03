package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger-only wrapper for a permanent event that also has an intervening condition.
 * The event permanent must match {@code predicate}, and the trigger collector checks
 * {@code condition} when the event occurs before queuing the wrapped effect.
 */
public record ConditionalTriggeringPermanentEffect(
        Condition condition,
        PermanentPredicate predicate,
        CardEffect wrapped
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
