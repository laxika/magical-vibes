package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Creates one token for each named counter on the source permanent under that permanent's owner's
 * control after it leaves the battlefield.
 */
public record CreateTokensForEachLeavingSourceCounterForOwnerEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect {
}
