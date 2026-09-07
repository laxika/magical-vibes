package com.github.laxika.magicalvibes.model.condition;

/** The targeted graveyard card's mana value is at least the specified amount. */
public record TargetGraveyardCardManaValueAtLeast(int minManaValue) implements Condition {

    @Override
    public String conditionName() {
        return "target graveyard card's mana value is at least " + minManaValue;
    }

    @Override
    public String conditionNotMetReason() {
        return "target graveyard card's mana value is less than " + minManaValue;
    }
}
