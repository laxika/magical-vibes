package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles every creature, then has each player create one token and put counters on it equal to
 * the total effective power of that player's creatures exiled this way.
 */
public record ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect(
        CreateTokenEffect tokenTemplate,
        CounterType counterType
) implements CardEffect {
}
