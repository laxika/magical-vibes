package com.github.laxika.magicalvibes.model.condition;

/** Whether a player attacked one of the controller's opponents with a matching creature. */
public record PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber()
        implements Condition {

    @Override
    public String conditionName() {
        return "a player attacks one of your opponents with a creature whose power or toughness equals the chosen number";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching creature attacked one of your opponents";
    }
}
