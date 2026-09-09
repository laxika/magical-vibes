package com.github.laxika.magicalvibes.model.condition;

/** The controller controls a Villain with greater mana value than the target permanent. */
public record ControlsVillainWithGreaterManaValueThanTarget() implements Condition {

    @Override
    public String conditionName() {
        return "controls a Villain with greater mana value than the target";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a Villain with greater mana value than the target";
    }
}
