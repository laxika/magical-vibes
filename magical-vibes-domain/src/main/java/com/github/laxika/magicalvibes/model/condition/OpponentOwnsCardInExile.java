package com.github.laxika.magicalvibes.model.condition;

/** An opponent of the controller owns at least one card in exile. */
public record OpponentOwnsCardInExile() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent owns a card in exile";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent owns a card in exile";
    }
}
