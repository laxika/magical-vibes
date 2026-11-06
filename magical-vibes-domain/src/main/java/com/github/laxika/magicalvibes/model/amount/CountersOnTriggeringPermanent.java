package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * The number of {@code counterType} counters ("for each +1/+1 counter on it") the permanent the resolving trigger's
 * event was about had as it last existed on the battlefield (CR 603.10a): the stack entry's triggering permanent.
 * A {@code null} type counts every counter on it ("for each counter on it"). 0 when the entry has no triggering
 * permanent.
 *
 * @param counterType the counter type to count, or {@code null} for all of them
 */
public record CountersOnTriggeringPermanent(CounterType counterType) implements DynamicAmount {

    /** Every counter on the triggering permanent. */
    public CountersOnTriggeringPermanent() {
        this(null);
    }
}
