package com.github.laxika.magicalvibes.model.condition;

/** Matches when an opponent of the source controller lost life during the immediately preceding turn. */
public record OpponentLostLifeLastTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent lost life last turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent lost life last turn";
    }
}
