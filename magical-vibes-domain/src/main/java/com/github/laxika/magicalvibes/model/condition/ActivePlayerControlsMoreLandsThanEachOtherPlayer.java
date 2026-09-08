package com.github.laxika.magicalvibes.model.condition;

/** True when the active player controls strictly more lands than every other player. */
public record ActivePlayerControlsMoreLandsThanEachOtherPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "the active player controls more lands than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "the active player does not control more lands than each other player";
    }
}
