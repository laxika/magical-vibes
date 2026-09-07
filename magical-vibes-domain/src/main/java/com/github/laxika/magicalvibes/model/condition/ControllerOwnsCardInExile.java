package com.github.laxika.magicalvibes.model.condition;

/** The controller owns at least one card in exile. */
public record ControllerOwnsCardInExile() implements Condition {

    @Override
    public String conditionName() {
        return "you own a card in exile";
    }

    @Override
    public String conditionNotMetReason() {
        return "you don't own a card in exile";
    }
}
