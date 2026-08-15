package com.github.laxika.magicalvibes.model.condition;

/** True when the controller has more permanents than every other player. */
public record ControllerControlsMorePermanentsThanEachOtherPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "you control more permanents than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not control more permanents than each other player";
    }
}
