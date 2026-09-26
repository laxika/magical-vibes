package com.github.laxika.magicalvibes.model.condition;

/** The targeted graveyard card's mana value is at most its controller's experience counters. */
public record TargetGraveyardCardManaValueAtMostControllerExperienceCounters() implements Condition {

    @Override
    public String conditionName() {
        return "target graveyard card's mana value is at most your experience counters";
    }

    @Override
    public String conditionNotMetReason() {
        return "target graveyard card's mana value is greater than your experience counters";
    }
}
