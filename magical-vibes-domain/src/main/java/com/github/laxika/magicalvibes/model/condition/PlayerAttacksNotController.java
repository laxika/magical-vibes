package com.github.laxika.magicalvibes.model.condition;

/** Whether the attacking player did not attack this condition's controller directly. */
public record PlayerAttacksNotController() implements Condition {

    @Override
    public String conditionName() {
        return "the attacking player isn't attacking you";
    }

    @Override
    public String conditionNotMetReason() {
        return "the attacking player is attacking you";
    }
}
