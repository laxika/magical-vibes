package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller controls strictly more creatures than the player on the other side of the combat: for "can't attack
 * unless" the defending player of each possible attack (CR 508.5), for "can't block unless" the attacking player.
 */
public record ControlsMoreCreaturesThanOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "control more creatures than the opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "do not control more creatures than the opponent";
    }
}
