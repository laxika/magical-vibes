package com.github.laxika.magicalvibes.model.effect;

/**
 * How long an effect lasts.
 */
public enum EffectDuration {
    /** Permanent static/continuous effect (e.g. auras, equipment). */
    CONTINUOUS,
    /** One-shot effect that wears off at end of turn (cleared by resetModifiers). */
    UNTIL_END_OF_TURN
}
