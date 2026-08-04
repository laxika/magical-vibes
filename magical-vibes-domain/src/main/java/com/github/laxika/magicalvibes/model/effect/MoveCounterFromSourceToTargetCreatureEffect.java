package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Move a [type] counter from this permanent onto target creature" (e.g. Afiya Grove).
 *
 * <p>Moving is a removal plus a placement: if the source permanent is gone or has no counter of the
 * type left, nothing is removed and nothing is placed.</p>
 *
 * @param counterType the kind of counter moved
 */
public record MoveCounterFromSourceToTargetCreatureEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
