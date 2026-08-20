package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller of the source permanent dealt damage to the target permanent this turn
 * with a Giant, Wizard, or spell.
 */
public record GiantWizardOrSpellDealtDamageToTargetThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a Giant, Wizard, or spell you controlled dealt damage to it this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no Giant, Wizard, or spell you controlled dealt damage to it this turn";
    }
}
