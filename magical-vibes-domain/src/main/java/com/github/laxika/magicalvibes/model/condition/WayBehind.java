package com.github.laxika.magicalvibes.model.condition;

/** The controller was at least ten life, three creatures, or three cards behind an opponent this turn. */
public record WayBehind() implements Condition {

    @Override
    public String conditionName() {
        return "way behind";
    }

    @Override
    public String conditionNotMetReason() {
        return "you are not way behind";
    }
}
