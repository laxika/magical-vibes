package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

public record TransformSelfThenEffect(List<CardEffect> effectsOnTransform) implements CardEffect {

    public TransformSelfThenEffect {
        effectsOnTransform = List.copyOf(effectsOnTransform);
    }

    public TransformSelfThenEffect(CardEffect... effects) {
        this(List.of(effects));
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
