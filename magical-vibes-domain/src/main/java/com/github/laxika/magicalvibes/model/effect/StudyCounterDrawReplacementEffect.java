package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Pursuit of Knowledge's optional replacement for a draw.
 */
public record StudyCounterDrawReplacementEffect() implements CounterDrawReplacementEffect {

    @Override
    public CounterType counterType() {
        return CounterType.STUDY;
    }
}
