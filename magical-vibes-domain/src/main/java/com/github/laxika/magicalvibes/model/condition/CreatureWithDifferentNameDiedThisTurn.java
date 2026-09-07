package com.github.laxika.magicalvibes.model.condition;

/** A creature whose last-known battlefield name differs from the excluded name died this turn. */
public record CreatureWithDifferentNameDiedThisTurn(String excludedName) implements Condition {

    @Override
    public String conditionName() {
        return "a creature with a different name died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature with a different name died this turn";
    }
}
