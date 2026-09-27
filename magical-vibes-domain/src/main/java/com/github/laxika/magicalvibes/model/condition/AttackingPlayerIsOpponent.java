package com.github.laxika.magicalvibes.model.condition;

/** Whether the player whose attack caused this trigger is an opponent of the source controller. */
public record AttackingPlayerIsOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "the attacking player is an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "the attacking player is not an opponent";
    }
}
