package com.github.laxika.magicalvibes.model.condition;

/** The source card is currently represented by a face-up card in exile. */
public record SourceCardInExile() implements Condition {

    @Override
    public String conditionName() {
        return "source card is in exile";
    }

    @Override
    public String conditionNotMetReason() {
        return "source card is not in exile";
    }
}
