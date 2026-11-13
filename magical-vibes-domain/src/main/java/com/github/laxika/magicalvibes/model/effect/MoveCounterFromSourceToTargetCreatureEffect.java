package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
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
 * @param anotherTarget whether the target is "another target creature", never the source itself
 */
public record MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                          boolean anotherTarget)
        implements CardEffect {

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, amount, false);
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType) {
        this(counterType, new Fixed(1));
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, int amount) {
        this(counterType, new Fixed(amount));
    }

    /** "Move [amount] counters from this creature onto another target creature" (Tester of the Tangential). */
    public static MoveCounterFromSourceToTargetCreatureEffect ontoAnotherCreature(CounterType counterType,
                                                                                 DynamicAmount amount) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, amount, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return anotherTarget
                ? TargetSpec.benign(TargetPredicates.creature(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
