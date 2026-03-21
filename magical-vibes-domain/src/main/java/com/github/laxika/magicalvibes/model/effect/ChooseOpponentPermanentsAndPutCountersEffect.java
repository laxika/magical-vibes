package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When the source permanent enters the battlefield, choose up to {@code maxCount}
 * permanents that opponents control matching the given {@code filter}, and put a
 * counter of type {@code counterType} on each of them.
 *
 * <p>If the number of eligible permanents is less than or equal to {@code maxCount},
 * counters are placed on all of them automatically. If more eligible permanents exist,
 * the controller chooses exactly {@code maxCount} via multi-permanent choice.
 *
 * <p>Used by Haphazard Bombardment: choose four nonenchantment permanents you don't
 * control and put an aim counter on each of them.
 */
public record ChooseOpponentPermanentsAndPutCountersEffect(
        CounterType counterType,
        int maxCount,
        PermanentPredicate filter
) implements CardEffect {
}
