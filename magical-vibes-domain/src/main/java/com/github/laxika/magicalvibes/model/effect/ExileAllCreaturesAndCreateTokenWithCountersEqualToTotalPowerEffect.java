package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every creature, then has each player create one token with +1/+1 counters on it equal to
 * the total effective power of that player's creatures exiled this way.
 */
public record ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect(
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
