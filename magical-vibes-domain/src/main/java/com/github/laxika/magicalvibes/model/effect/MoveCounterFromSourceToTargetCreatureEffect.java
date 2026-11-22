package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Move a [type] counter from this permanent onto target creature" (e.g. Afiya Grove), and the
 * "move X counters" form (e.g. Tester of the Tangential, whose
 * reflexive ability reads the X paid). The other direction, "move a counter from target creature onto this
 * creature" (Cytoplast Root-Kin), is {@link #fromTargetOntoSource}.
 *
 * <p>Moving is a removal plus a placement: if the source permanent is gone or has no counter of the
 * type left, nothing is removed and nothing is placed. When the source has fewer counters than
 * {@code amount}, only the counters it has are moved. The moved counters are placed in one event.</p>
 *
 * @param counterType the kind of counter moved
 * @param amount      the number of counters to move, evaluated as the effect resolves
 * @param anotherTarget whether the target is "another target creature", never the source itself
 * @param from          whose counters move: {@link Subjects#source()} or the target
 * @param to            where they are put: the target or {@link Subjects#source()}, whichever {@code from} is not
 * @param anyPermanent  whether the target is any permanent rather than a creature (the ability's filter narrows it)
 */
public record MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                          boolean anotherTarget, PermanentSubject from,
                                                          PermanentSubject to, boolean anyPermanent)
        implements CardEffect {

    public MoveCounterFromSourceToTargetCreatureEffect {
        if (!(from.equals(Subjects.source()) && to.equals(Subjects.target()))
                && !(from.equals(Subjects.target()) && to.equals(Subjects.source()))) {
            throw new IllegalArgumentException("Counters move between the source and the target: " + from + ", " + to);
        }
        if (anotherTarget && anyPermanent) {
            throw new IllegalArgumentException("\"Another target\" is a creature here");
        }
    }

    public MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType, DynamicAmount amount,
                                                       boolean anotherTarget) {
        this(counterType, amount, anotherTarget, Subjects.source(), Subjects.target(), false);
    }

    /** "Move a [type] counter from target creature onto this creature" (Arcbound Fiend). */
    public static MoveCounterFromSourceToTargetCreatureEffect fromTargetOntoSource(CounterType counterType) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, new Fixed(1), false,
                Subjects.target(), Subjects.source(), false);
    }

    /** "Move [amount] counters from this permanent onto target [permanent]" (Blaster, Morale Booster). */
    public static MoveCounterFromSourceToTargetCreatureEffect ontoTargetPermanent(CounterType counterType,
                                                                                 DynamicAmount amount) {
        return new MoveCounterFromSourceToTargetCreatureEffect(counterType, amount, false,
                Subjects.source(), Subjects.target(), true);
    }

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
        if (anotherTarget) {
            return TargetSpec.benign(TargetPredicates.creature(),
                    new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
        }
        return TargetSpec.benign(anyPermanent ? TargetPredicates.permanent() : TargetPredicates.creature());
    }
}
