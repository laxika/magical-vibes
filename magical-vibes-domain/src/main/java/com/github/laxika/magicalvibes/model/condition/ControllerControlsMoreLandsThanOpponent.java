package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller controls strictly more lands than the player on the other side of the combat: for "can't attack
 * unless" the defending player of each possible attack (CR 508.5), for "can't block unless" the attacking player.
 */
public record ControllerControlsMoreLandsThanOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "you control more lands than your opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "you control no more lands than your opponent";
    }
}
