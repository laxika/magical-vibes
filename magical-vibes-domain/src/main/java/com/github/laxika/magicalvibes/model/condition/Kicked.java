package com.github.laxika.magicalvibes.model.condition;

/** The spell or permanent was kicked (MTG Rule 702.32c). */
public record Kicked() implements Condition {

    @Override
    public String conditionName() {
        return "kicked";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was not kicked";
    }
}
