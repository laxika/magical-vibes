package com.github.laxika.magicalvibes.model.condition;

/** Exactly {@code attackerCount} creatures are attacking. */
public record ExactlyAttackers(int attackerCount) implements Condition {

    @Override
    public String conditionName() {
        return "exactly " + attackerCount + " attackers";
    }

    @Override
    public String conditionNotMetReason() {
        return "the number of attacking creatures was not exactly " + attackerCount;
    }
}
