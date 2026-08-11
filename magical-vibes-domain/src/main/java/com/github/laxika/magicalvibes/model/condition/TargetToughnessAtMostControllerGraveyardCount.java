package com.github.laxika.magicalvibes.model.condition;

/** The targeted permanent's effective toughness is at most the controller's graveyard size. */
public record TargetToughnessAtMostControllerGraveyardCount() implements Condition {

    @Override
    public String conditionName() {
        return "target's toughness is at most the number of cards in your graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "target's toughness is greater than the number of cards in your graveyard";
    }
}
