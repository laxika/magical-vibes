package com.github.laxika.magicalvibes.model.condition;

/** Whether the named plane was entered by planeswalking during the current turn. */
public record PlaneswalkedToPlaneThisTurn(String planeName) implements Condition {

    @Override
    public String conditionName() {
        return "planeswalked to " + planeName + " this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you did not planeswalk to " + planeName + " this turn";
    }
}
