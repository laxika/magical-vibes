package com.github.laxika.magicalvibes.model.condition;

/** The source card is represented by a permanent on the battlefield. */
public record SourceCardOnBattlefield() implements Condition {

    @Override
    public String conditionName() {
        return "source card is on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "source card is not on the battlefield";
    }
}
