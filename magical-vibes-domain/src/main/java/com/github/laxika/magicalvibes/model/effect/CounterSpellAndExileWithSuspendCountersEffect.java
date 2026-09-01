package com.github.laxika.magicalvibes.model.effect;

/** Counters the target spell and exiles it with suspend time counters. */
public record CounterSpellAndExileWithSuspendCountersEffect(int counters)
        implements CounterSpellingEffect {

    public CounterSpellAndExileWithSuspendCountersEffect {
        if (counters <= 0) {
            throw new IllegalArgumentException("counters must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
