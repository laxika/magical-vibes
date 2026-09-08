package com.github.laxika.magicalvibes.model.condition;

/** Met during the active player's declare blockers step when the controller is that player. */
public record DuringYourDeclareBlockers() implements Condition {

    @Override
    public String conditionName() {
        return "during your declare blockers step";
    }

    @Override
    public String conditionNotMetReason() {
        return "not during your declare blockers step";
    }
}
