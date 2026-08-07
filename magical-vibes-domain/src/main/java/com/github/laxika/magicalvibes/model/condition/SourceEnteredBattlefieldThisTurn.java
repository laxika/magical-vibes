package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent entered the battlefield this turn. Unlike {@link CameUnderControlThisTurn},
 * which reads the summoning-sickness flag and therefore resets when the permanent changes
 * controller, this scans {@code GameData.permanentsEnteredBattlefieldThisTurn}, so it is true only
 * for the turn the permanent actually entered. Used by Fungus Elemental's "Activate only if this
 * creature entered this turn".
 */
public record SourceEnteredBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "this permanent entered the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it did not enter the battlefield this turn";
    }
}
