package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Put a {@code counterType} counter on this permanent. Then you may pay {X}, where X is the
 * number of {@code counterType} counters on it. If you don't, tap this permanent and it deals X
 * damage to you." Modelled as a single triggered effect because both halves are one ability whose
 * X depends on the counter added first (Primordial Ooze).
 *
 * <p>Resolution places the counter, snapshots X, then reuses the {@code ForcedCostOrElse}
 * "you may pay {X}; if you don't, [tap self + deal X to controller]" machinery.
 */
public record AddCounterThenPayCountersOrTapAndDamageEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
