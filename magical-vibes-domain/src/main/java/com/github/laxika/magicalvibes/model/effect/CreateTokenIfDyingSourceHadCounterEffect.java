package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Opponent-creature-death trigger that creates one token if the dying creature had a named counter.
 *
 * @param counterType the counter type checked on the dying creature
 * @param tokenTemplate the token to create
 */
public record CreateTokenIfDyingSourceHadCounterEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
