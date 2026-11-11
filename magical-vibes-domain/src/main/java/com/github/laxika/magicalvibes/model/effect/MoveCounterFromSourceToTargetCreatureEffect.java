package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Move a [type] counter from this permanent onto target creature" (e.g. Afiya Grove), and the
 * "move X counters" form (e.g. Tester of the Tangential, whose
 * reflexive ability reads the X paid).
 *
 * <p>Moving is a removal plus a placement: if the source permanent is gone or has no counter of the
 * type left, nothing is removed and nothing is placed. When the source has fewer counters than
 * {@code amount}, only the counters it has are moved. The moved counters are placed in one event.</p>
 *
 * @param counterType the kind of counter moved
 * @param amount      the number of counters to move, evaluated as the effect resolves
 */
public record MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount)
        implements CardEffect {

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType) {
        this(counterType, new Fixed(1));
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, int amount) {
        this(counterType, new Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
