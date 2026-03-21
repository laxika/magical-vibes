package com.github.laxika.magicalvibes.model.effect;

/**
 * How long an effect lasts.
 */
public enum EffectDuration {
    /** Permanent static/continuous effect (e.g. auras, equipment). */
    CONTINUOUS,
    /** One-shot effect that wears off at end of turn (cleared by resetModifiers). */
    UNTIL_END_OF_TURN,
    /** One-shot effect that lasts until the beginning of the controller's next turn.
     *  Survives end-of-turn cleanup; cleared at the start of the controller's next turn. */
    UNTIL_YOUR_NEXT_TURN
}
