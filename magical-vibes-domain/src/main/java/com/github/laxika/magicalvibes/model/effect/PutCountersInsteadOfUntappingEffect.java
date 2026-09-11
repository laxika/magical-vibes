package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Puts counters on matching permanents instead of letting them untap during their untap step. */
public record PutCountersInsteadOfUntappingEffect(
        CounterType counterType,
        int replacementCount,
        PermanentPredicate filter
) implements UntapStepReplacementEffect {

    @Override
    public boolean sourceControllerOnly() {
        return true;
    }

    @Override
    public boolean removesCounters() {
        return false;
    }
}
