package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger that creates one token with +1/+1 counters equal to the dying source's power.
 *
 * @param tokenTemplate the token to create; its initial +1/+1 counter count is replaced
 */
public record CreateTokenWithDyingSourcePowerCountersEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
