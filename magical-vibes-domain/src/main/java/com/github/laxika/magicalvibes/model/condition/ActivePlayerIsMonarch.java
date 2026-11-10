package com.github.laxika.magicalvibes.model.condition;

/**
 * The player whose turn it is is the monarch: "at the beginning of the monarch's end step" (Archivist of Gondor),
 * the trigger's event condition.
 */
public record ActivePlayerIsMonarch() implements Condition {

    @Override
    public String conditionName() {
        return "the active player is the monarch";
    }

    @Override
    public String conditionNotMetReason() {
        return "the active player is not the monarch";
    }
}
