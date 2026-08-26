package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Removes one counter from the source permanent and triggers its sacrifice if that was the last. */
public record RemoveCounterAndSacrificeSelfOnLastEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
