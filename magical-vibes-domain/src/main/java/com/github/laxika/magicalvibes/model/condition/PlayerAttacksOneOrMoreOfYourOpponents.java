package com.github.laxika.magicalvibes.model.condition;

/**
 * Whether the attacking player attacked one or more of this condition's controller's opponents.
 * As the event condition of {@code Triggers.playerAttacks()} ("whenever a player attacks one or more of your
 * opponents") it triggers once per attacking player per combat, however many of the opponents were attacked, where
 * {@code Triggers.playerAttacksOneOfYourOpponents()} triggers once for each one.
 */
public record PlayerAttacksOneOrMoreOfYourOpponents() implements Condition {

    @Override
    public String conditionName() {
        return "a player attacks one or more of your opponents";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player attacked one or more of your opponents";
    }
}
