package com.github.laxika.magicalvibes.model.condition;

/** The targeted graveyard card's mana value is at most the specified amount. */
public record TargetGraveyardCardManaValueAtMost(int maxManaValue) implements Condition {

    @Override
    public String conditionName() {
        return "target graveyard card's mana value is at most " + maxManaValue;
    }

    @Override
    public String conditionNotMetReason() {
        return "target graveyard card's mana value is greater than " + maxManaValue;
    }
}
