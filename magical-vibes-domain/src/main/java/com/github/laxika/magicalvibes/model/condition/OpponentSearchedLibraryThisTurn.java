package com.github.laxika.magicalvibes.model.condition;

/** Matches when at least one opponent of the controller searched their own library this turn. */
public record OpponentSearchedLibraryThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent searched their library this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent searched their library this turn";
    }
}
