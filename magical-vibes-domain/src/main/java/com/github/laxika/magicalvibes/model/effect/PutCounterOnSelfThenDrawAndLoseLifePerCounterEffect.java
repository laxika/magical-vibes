package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;

/**
 * Puts one counter on the source permanent, then draws and loses life equal to the total number
 * of counters on it.
 */
public record PutCounterOnSelfThenDrawAndLoseLifePerCounterEffect(CounterType counterType)
        implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new TotalCountersOnSource();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
