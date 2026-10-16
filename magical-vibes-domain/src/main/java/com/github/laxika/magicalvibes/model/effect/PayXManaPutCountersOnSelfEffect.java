package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * On resolution, the controller may pay {X}: they choose X up to their available mana, pay it,
 * and put X counters of the specified type on the source permanent. Choosing X=0 declines.
 * The optional triggering-permanent form ("you may pay {X}. When you do, put X counters on that
 * permanent") instead puts the counters on the permanent that caused the trigger through a separate
 * reflexive trigger, so the counters are placed only after the payment is made.
 */
public record PayXManaPutCountersOnSelfEffect(CounterType counterType, boolean useTriggeringPermanent)
        implements CardEffect {

    public PayXManaPutCountersOnSelfEffect(CounterType counterType) {
        this(counterType, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
