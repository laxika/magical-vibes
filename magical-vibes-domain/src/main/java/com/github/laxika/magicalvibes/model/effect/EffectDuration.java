package com.github.laxika.magicalvibes.model.effect;

/**
 * How long an effect lasts.
 */
public enum EffectDuration {
    /** Permanent static/continuous effect (e.g. auras, equipment). */
    CONTINUOUS,
    /** One-shot effect that wears off at end of turn (cleared by resetModifiers). */
    UNTIL_END_OF_TURN,
    /** One-shot effect that wears off when the combat phase ends (e.g. Jade Statue's animation).
     *  Cleared by {@link com.github.laxika.magicalvibes.model.Permanent#clearCombatState()}. */
    UNTIL_END_OF_COMBAT,
    /** One-shot effect that lasts until the beginning of the controller's next turn.
     *  Survives end-of-turn cleanup; cleared at the start of the controller's next turn. */
    UNTIL_YOUR_NEXT_TURN,
    /** One-shot effect with no wear-off (e.g. "becomes a creature permanently" — Tezzeret, Waker). */
    PERMANENT,
    /** One-shot effect that lasts for as long as the source permanent remains on the battlefield
     *  (e.g. Awakener Druid); removed when the source leaves. */
    WHILE_SOURCE_ON_BATTLEFIELD,
    /** Continuous effect that applies only while the source (an Aura or Equipment) remains
     *  attached to the affected permanent. Used by the CR 613 layer engine for floating
     *  continuous effects backed by an attachment (see
     *  {@link com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect}). */
    WHILE_ATTACHED
}
