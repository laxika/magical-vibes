package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller (of the effect's source) has no cards in hand. Used as the intervening-"if"
 * for activated abilities and triggers that gate on the controller's empty hand — "if you have
 * no cards in hand" (Idle Thoughts, Bloodhall Priest). Unlike {@link ActivePlayerHandEmpty}, this
 * reads the controller's hand regardless of whose turn it is, so it is correct for abilities
 * activated on any player's turn and for ETB/attack triggers.
 */
public record ControllerHandEmpty() implements Condition {

    @Override
    public String conditionName() {
        return "empty hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "you have cards in hand";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
