package com.github.laxika.magicalvibes.model.condition;

/** Whether the opponent whose attack caused the trigger attacked a different opponent of this condition's controller. */
public record OpponentAttacksAnotherOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "opponent attacks another one of your opponents";
    }

    @Override
    public String conditionNotMetReason() {
        return "opponent did not attack another one of your opponents";
    }
}
