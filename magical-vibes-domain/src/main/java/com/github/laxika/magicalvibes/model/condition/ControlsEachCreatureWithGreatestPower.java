package com.github.laxika.magicalvibes.model.condition;

/**
 * True when every creature on the battlefield whose effective power ties the greatest effective
 * power among all creatures is controlled by the source's controller — "if you control each
 * creature on the battlefield with the greatest power" (Might Makes Right). Stricter than
 * {@link ControlsCreatureWithGreatestPower}, which only requires the controller to hold one of the
 * tied creatures. Vacuously true while no creature is on the battlefield.
 */
public record ControlsEachCreatureWithGreatestPower() implements Condition {

    @Override
    public String conditionName() {
        return "you control each creature on the battlefield with the greatest power";
    }

    @Override
    public String conditionNotMetReason() {
        return "an opponent controls a creature with the greatest power";
    }
}
