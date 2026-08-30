package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts a counter on the source permanent, then creates a reflexive triggered ability that may
 * cast a matching instant or sorcery card from its controller's graveyard for free.
 */
public record PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect(
        CounterType counterType,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
