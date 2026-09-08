package com.github.laxika.magicalvibes.model.condition;

/** Whether the opponent whose attack caused the trigger attacked one or more planeswalkers controlled by this condition's controller. */
public record OpponentAttacksPlaneswalker() implements Condition {

    @Override
    public String conditionName() {
        return "opponent attacks one or more planeswalkers you control";
    }

    @Override
    public String conditionNotMetReason() {
        return "opponent did not attack a planeswalker you control";
    }
}
