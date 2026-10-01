package com.github.laxika.magicalvibes.model.condition;

/** The defending player controls strictly more lands than the attacking creature's controller. */
public record DefendingPlayerControlsMoreLandsThanController() implements Condition {

    @Override
    public String conditionName() {
        return "defending player controls more lands than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player does not control more lands than you";
    }
}
