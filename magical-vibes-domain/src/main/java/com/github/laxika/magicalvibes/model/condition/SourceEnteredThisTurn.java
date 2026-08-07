package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent entered the battlefield this turn — the intervening-"if" of
 * "… if this creature entered this turn" (Hixus, Prison Warden). Unlike
 * {@link CameUnderControlThisTurn} this reads the entered-this-turn record, so a permanent
 * whose control merely changed this turn does not satisfy it.
 */
public record SourceEnteredThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "entered this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it did not enter the battlefield this turn";
    }
}
