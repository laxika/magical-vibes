package com.github.laxika.magicalvibes.model.condition;

/** A non-Zombie creature died this turn. */
public record NonZombieCreatureDiedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a non-Zombie creature died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no non-Zombie creature died this turn";
    }
}
