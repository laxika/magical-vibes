package com.github.laxika.magicalvibes.model.effect;

/**
 * The step or phase a {@link SkipDrawStepEffect} makes a player skip. As a static ability: {@link #DRAW_STEP} and
 * {@link #UPKEEP_STEP}. Floated on a player for a duration ("skips each instance of the chosen step or phase this
 * turn", "skip all combat phases of your next turn"): {@link #DRAW_STEP}, {@link #MAIN_PHASE} and
 * {@link #COMBAT_PHASE}.
 */
public enum SkipStepOrPhaseKind {
    DRAW_STEP,
    MAIN_PHASE,
    COMBAT_PHASE,
    UPKEEP_STEP
}
