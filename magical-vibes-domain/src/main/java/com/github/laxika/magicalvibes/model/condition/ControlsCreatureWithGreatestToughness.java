package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller controls a creature whose effective toughness is greater than or equal
 * to the greatest effective toughness among all creatures on the battlefield.
 */
public record ControlsCreatureWithGreatestToughness() implements Condition {

    @Override
    public String conditionName() {
        return "you control the creature with the greatest toughness or tied for the greatest toughness";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not control a creature with the greatest toughness";
    }
}
