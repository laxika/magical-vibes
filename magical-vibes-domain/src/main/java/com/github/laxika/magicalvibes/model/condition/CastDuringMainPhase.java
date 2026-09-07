package com.github.laxika.magicalvibes.model.condition;

/** The spell was cast during its controller's precombat or postcombat main phase. */
public record CastDuringMainPhase() implements Condition {

    @Override
    public String conditionName() {
        return "cast during the controller's main phase";
    }

    @Override
    public String conditionNotMetReason() {
        return "not cast during the controller's main phase";
    }
}
