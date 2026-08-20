package com.github.laxika.magicalvibes.model.condition;

/**
 * True if the source card was its controller's second spell cast this turn.
 */
public record SourceWasSecondSpellCastThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source was your second spell this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this was not your second spell this turn";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
