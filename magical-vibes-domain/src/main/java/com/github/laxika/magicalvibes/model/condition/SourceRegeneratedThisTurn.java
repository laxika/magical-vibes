package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the source permanent regenerated at least once this turn (CR 701.15). Used as the
 * intervening-if of Spiny Starfish's "At the beginning of each end step, if this creature
 * regenerated this turn, …".
 */
public record SourceRegeneratedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "regenerated this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it did not regenerate this turn";
    }
}
