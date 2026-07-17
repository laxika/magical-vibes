package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Puts up to {@code amount} counters of {@code counterType} on the source permanent, but never
 * enough to raise the total number of that counter type above {@code cap}. "Put up to X [type]
 * counters on this creature. This ability can't cause the total number of [type] counters on this
 * creature to be greater than {@code cap}." (e.g. Clockwork Beast's {X}, {T} upkeep ability, cap 7).
 * {@code amount} is a {@link DynamicAmount} (typically {@code XValue()} for the X paid).
 */
public record PutCappedCountersOnSourceEffect(CounterType counterType, DynamicAmount amount, int cap)
        implements CardEffect {
}
