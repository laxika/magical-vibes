package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;
import java.util.UUID;

/**
 * In active-player-first order, each player may put counters on a creature they control. A player
 * who does gets an attack restriction against the effect controller until that controller's next turn.
 */
public record EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
        CounterType counterType, int count, List<UUID> remainingPlayerIds, UUID sourceControllerId)
        implements CardEffect {

    public EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(CounterType counterType, int count) {
        this(counterType, count, List.of(), null);
    }

    public EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
    }
}
