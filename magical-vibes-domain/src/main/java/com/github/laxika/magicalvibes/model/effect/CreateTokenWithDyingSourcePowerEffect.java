package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger that creates one token whose base power and toughness equal the dying source's
 * power. The token gets no counters.
 *
 * @param tokenTemplate the token to create; its power and toughness are replaced by the dying power
 */
public record CreateTokenWithDyingSourcePowerEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
