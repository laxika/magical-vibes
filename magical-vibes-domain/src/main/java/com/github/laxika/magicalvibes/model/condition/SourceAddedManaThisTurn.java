package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has already added mana with this ability this turn. */
public record SourceAddedManaThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source added mana with this ability this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has not added mana with this ability this turn";
    }
}
