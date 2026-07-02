package com.github.laxika.magicalvibes.model.condition;

/** The spell or permanent was NOT kicked (MTG Rule 702.32). */
public record NotKicked() implements Condition {

    @Override
    public String conditionName() {
        return "wasn't kicked";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was kicked";
    }
}
