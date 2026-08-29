package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts counters on each creature controlled by the resolving effect's controller equal to that
 * creature's effective power minus its current base power, when the difference is positive.
 */
public record PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect(CounterType counterType)
        implements CardEffect {
}
