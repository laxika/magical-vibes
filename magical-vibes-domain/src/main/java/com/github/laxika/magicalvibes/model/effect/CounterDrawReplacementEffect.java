package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static replacement effect that may put a counter on its source instead of a draw.
 */
public interface CounterDrawReplacementEffect extends CardEffect {

    CounterType counterType();
}
