package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * On resolution, the controller may pay {X}: they choose X up to their available mana, pay it,
 * and put X counters of the specified type on the source permanent. Choosing X=0 declines.
 * <p>
 * With {@code reflexive}, the payment is followed by a separate reflexive trigger ("you may pay
 * {X}. When you do, put X counters on ...") so the counters are placed only after the payment is
 * made. {@code useTriggeringPermanent} selects the permanent that caused the trigger as the target
 * of that reflexive trigger instead of the source permanent (Remnant of the Rising Star).
 */
public record PayXManaPutCountersOnSelfEffect(CounterType counterType, boolean useTriggeringPermanent,
                                              boolean reflexive)
        implements CardEffect {

    public PayXManaPutCountersOnSelfEffect(CounterType counterType, boolean useTriggeringPermanent) {
        this(counterType, useTriggeringPermanent, useTriggeringPermanent);
    }

    public PayXManaPutCountersOnSelfEffect(CounterType counterType) {
        this(counterType, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
