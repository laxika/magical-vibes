package com.github.laxika.magicalvibes.model.condition;

/**
 * Whether the attacking player attacked one or more of this condition's controller's opponents.
 * Unlike {@link PlayerAttacksOneOfYourOpponents}, a trigger gated on this condition fires once per
 * attacking player per combat, regardless of how many distinct opponents were attacked.
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
