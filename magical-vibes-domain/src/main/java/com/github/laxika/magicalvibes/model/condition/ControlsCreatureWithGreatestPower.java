package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller controls a creature whose effective power is greater than or equal to
 * the greatest effective power among all creatures on the battlefield — "if you control the
 * creature with the greatest power or tied for the greatest power" (Triumph of Cruelty).
 * Never met while the controller controls no creatures.
 */
public record ControlsCreatureWithGreatestPower() implements Condition {

    @Override
    public String conditionName() {
        return "you control the creature with the greatest power or tied for the greatest power";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not control a creature with the greatest power";
    }
}
