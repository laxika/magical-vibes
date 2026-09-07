package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Creates a source-independent continuous effect that grants a subtype to the target while it
 * has a counter of the given type. The target is supplied by a sibling targeting effect.
 */
public record GrantSubtypeToTargetWhileHasCounterEffect(CardSubtype subtype, CounterType counterType)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
