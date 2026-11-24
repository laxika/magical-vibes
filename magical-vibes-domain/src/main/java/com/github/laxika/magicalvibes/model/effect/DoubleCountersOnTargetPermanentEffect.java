package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Doubles counters on the permanents {@code subject} names: target permanent (the default; Gilder Bairn and
 * Invigorating Surge), {@link Subjects#source()} ("double the number of +1/+1 counters on this creature", Ascendant
 * Acolyte, The Millennium Calendar) or a set named without targeting ("double the number of +1/+1 counters on each
 * creature you control", Kalonian Hydra: {@link Subjects#creaturesYouControl()}; "each kind of counter on each Spider
 * and legendary creature you control", Ultimate Spider-Man: a {@link Subjects#controlledBy} set). A null counter type
 * doubles every kind; otherwise only the specified kind is doubled. Doubling counters is putting counters
 * (CR 701.10e), so each permanent's counters go through the placement replacements and triggers one by one.
 */
public record DoubleCountersOnTargetPermanentEffect(CounterType counterType, PermanentSubject subject)
        implements CardEffect, DoublingEffect {

    public DoubleCountersOnTargetPermanentEffect {
        if (subject == null) {
            subject = Subjects.target();
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
