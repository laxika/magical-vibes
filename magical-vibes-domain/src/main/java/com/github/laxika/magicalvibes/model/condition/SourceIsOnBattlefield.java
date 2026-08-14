package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is still on the battlefield. */
public record SourceIsOnBattlefield() implements Condition {

    @Override
    public String conditionName() {
        return "source is on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is no longer on the battlefield";
    }
}
