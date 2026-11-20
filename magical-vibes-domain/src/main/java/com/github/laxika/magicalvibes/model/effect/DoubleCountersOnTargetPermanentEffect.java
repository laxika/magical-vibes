package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Doubles counters on the permanent {@code subject} names: target permanent (the default; Gilder Bairn and
 * Invigorating Surge) or {@link Subjects#source()} ("double the number of +1/+1 counters on this creature", Ascendant
 * Acolyte, The Millennium Calendar). A null counter type doubles every kind; otherwise only the specified kind is
 * doubled. Doubling counters is putting counters (CR 701.10e).
 */
public record DoubleCountersOnTargetPermanentEffect(CounterType counterType, PermanentSubject subject)
        implements CardEffect, DoublingEffect {

    public DoubleCountersOnTargetPermanentEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if (!(subject instanceof PermanentSubject.Target) && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Doubles counters on the target or the source: " + subject);
        }
    }

    public DoubleCountersOnTargetPermanentEffect(CounterType counterType) {
        this(counterType, Subjects.target());
    }

    public DoubleCountersOnTargetPermanentEffect() {
        this(null, Subjects.target());
    }

    /** "Double the number of {@code counterType} counters on this permanent." */
    public static DoubleCountersOnTargetPermanentEffect onSource(CounterType counterType) {
        return new DoubleCountersOnTargetPermanentEffect(counterType, Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
