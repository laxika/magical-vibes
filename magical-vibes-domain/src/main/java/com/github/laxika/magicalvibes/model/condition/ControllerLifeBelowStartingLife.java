package com.github.laxika.magicalvibes.model.condition;

/** The controller's life total is less than the starting life total of the game's format. */
public record ControllerLifeBelowStartingLife() implements Condition {

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }

    @Override
    public String conditionName() {
        return "life less than the starting life total";
    }

    @Override
    public String conditionNotMetReason() {
        return "life total is not less than the starting life total";
    }
}
