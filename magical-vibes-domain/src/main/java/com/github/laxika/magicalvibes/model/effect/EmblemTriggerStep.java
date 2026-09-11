package com.github.laxika.magicalvibes.model.effect;

/** The step or phase an {@link EmblemStepTriggerEffect} fires in. */
public enum EmblemTriggerStep {

    /** "At the beginning of your upkeep, …" */
    UPKEEP,

    /** "At the beginning of each opponent's upkeep, …" */
    OPPONENT_UPKEEP,

    /** "At the beginning of your draw step, …" */
    DRAW_STEP,

    /** "At the beginning of each opponent's draw step, …" */
    OPPONENT_DRAW_STEP,

    /** "At the beginning of combat on your turn, …" */
    BEGINNING_OF_COMBAT,

    /** "At the beginning of your end step, …" */
    END_STEP,
    /** At the end of the first combat phase on your turn. */
    END_OF_FIRST_COMBAT
}
