package com.github.laxika.magicalvibes.model.effect;

/** The step an {@link EmblemStepTriggerEffect} fires in, always on the emblem controller's turn. */
public enum EmblemTriggerStep {

    /** "At the beginning of your upkeep, …" */
    UPKEEP,

    /** "At the beginning of your end step, …" */
    END_STEP
}
