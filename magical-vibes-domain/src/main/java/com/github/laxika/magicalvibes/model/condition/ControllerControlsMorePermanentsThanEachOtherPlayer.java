package com.github.laxika.magicalvibes.model.condition;

/** True when the controller has more permanents than every other player. */
public record ControllerControlsMorePermanentsThanEachOtherPlayer(boolean requireDampingRestrictionActive)
        implements Condition {

    public ControllerControlsMorePermanentsThanEachOtherPlayer() {
        this(false);
    }

    @Override
    public String conditionName() {
        return "you control more permanents than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return requireDampingRestrictionActive
                ? "you must control more permanents than each other player and may ignore this effect only once each turn"
                : "you do not control more permanents than each other player";
    }
}
