package com.github.laxika.magicalvibes.model.condition;

/** The defending player (opponent of the controller) has at least one poison counter. */
public record DefendingPlayerPoisoned() implements Condition {

    @Override
    public String conditionName() {
        return "defending player poisoned";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player is not poisoned";
    }
}
