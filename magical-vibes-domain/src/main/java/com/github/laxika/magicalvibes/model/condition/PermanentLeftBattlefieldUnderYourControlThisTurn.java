package com.github.laxika.magicalvibes.model.condition;

/** A permanent left the battlefield under the controller's control this turn. */
public record PermanentLeftBattlefieldUnderYourControlThisTurn() implements Condition {

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }

    @Override
    public String conditionName() {
        return "a permanent left the battlefield under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent left the battlefield under your control this turn";
    }
}
