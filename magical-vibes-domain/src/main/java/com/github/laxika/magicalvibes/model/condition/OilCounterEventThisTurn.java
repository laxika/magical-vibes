package com.github.laxika.magicalvibes.model.condition;

/**
 * True when an oil counter was removed from a permanent controlled by the activating player, or
 * when a permanent with an oil counter on it was put into a graveyard, during the current turn.
 */
public record OilCounterEventThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an oil counter event happened this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no oil counter was removed from a permanent you controlled and no permanent with an oil counter on it was put into a graveyard this turn";
    }
}
