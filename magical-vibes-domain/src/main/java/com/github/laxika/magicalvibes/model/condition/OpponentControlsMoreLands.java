package com.github.laxika.magicalvibes.model.condition;

/**
 * An opponent controls strictly more lands than the controller (e.g. Gift of Estates:
 * "If an opponent controls more lands than you").
 */
public record OpponentControlsMoreLands() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent controls more lands";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls more lands than you";
    }
}
