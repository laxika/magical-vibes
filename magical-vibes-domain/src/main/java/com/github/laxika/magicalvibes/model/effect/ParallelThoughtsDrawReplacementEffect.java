package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/**
 * Static marker for Parallel Thoughts: its controller may replace a draw with the top card of the
 * face-down pile exiled with that enchantment.
 */
public record ParallelThoughtsDrawReplacementEffect() implements MaySkipDrawReplacementEffect {

    @Override
    public DrawReplacementKind replacementKind() {
        return DrawReplacementKind.PARALLEL_THOUGHTS;
    }
}
