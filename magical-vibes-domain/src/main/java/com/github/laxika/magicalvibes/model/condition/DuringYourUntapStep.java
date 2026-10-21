package com.github.laxika.magicalvibes.model.condition;

/**
 * Met during the untap step when the controller is the active player ("during your untap step").
 * Only meaningful as a trigger-time gate: the untap step has no priority, so a "becomes untapped"
 * ability goes on the stack in a later step and must be checked when the untap event happens.
 */
public record DuringYourUntapStep() implements Condition {

    @Override
    public String conditionName() {
        return "during your untap step";
    }

    @Override
    public String conditionNotMetReason() {
        return "not during your untap step";
    }
}
