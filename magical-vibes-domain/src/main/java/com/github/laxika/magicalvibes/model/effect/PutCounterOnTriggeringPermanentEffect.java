package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts {@code count} counters of {@code counterType} on the permanent whose event produced this
 * triggered ability — "put a wind counter on it" (Freyalise's Winds), where "it" is the permanent
 * that became tapped, not a chosen target.
 *
 * <p>The permanent is read from {@code StackEntry.triggeringPermanentId} at resolution, so this
 * effect belongs on a trigger slot that populates it (currently the becomes-tapped slots). It never
 * targets and never fizzles; if the permanent has left the battlefield, nothing happens.</p>
 */
public record PutCounterOnTriggeringPermanentEffect(CounterType counterType, int count) implements CardEffect {

    public PutCounterOnTriggeringPermanentEffect(CounterType counterType) {
        this(counterType, 1);
    }
}
