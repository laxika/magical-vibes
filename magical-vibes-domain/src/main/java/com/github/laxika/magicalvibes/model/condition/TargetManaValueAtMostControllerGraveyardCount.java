package com.github.laxika.magicalvibes.model.condition;

/** The targeted permanent's mana value is at most the controller's graveyard size. */
public record TargetManaValueAtMostControllerGraveyardCount() implements Condition {

    @Override
    public String conditionName() {
        return "target's mana value is at most the number of cards in your graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "target's mana value is greater than the number of cards in your graveyard";
    }
}
