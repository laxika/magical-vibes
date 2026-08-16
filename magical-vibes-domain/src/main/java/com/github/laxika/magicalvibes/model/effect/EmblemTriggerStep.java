package com.github.laxika.magicalvibes.model.effect;

/** The step an {@link EmblemStepTriggerEffect} fires in. */
public enum EmblemTriggerStep {

    /** "At the beginning of your upkeep, …" */
    UPKEEP,

    /** "At the beginning of each opponent's upkeep, …" */
    OPPONENT_UPKEEP,

    /** "At the beginning of your draw step, …" */
    DRAW_STEP,

    /** "At the beginning of combat on your turn, …" */
    BEGINNING_OF_COMBAT,

    /** "At the beginning of your end step, …" */
    END_STEP
}
