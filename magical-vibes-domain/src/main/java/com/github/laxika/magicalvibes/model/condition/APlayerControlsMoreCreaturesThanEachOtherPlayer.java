package com.github.laxika.magicalvibes.model.condition;

/** True when exactly one player controls more creatures than every other player. */
public record APlayerControlsMoreCreaturesThanEachOtherPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "a player controls more creatures than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player controls more creatures than each other player";
    }
}
