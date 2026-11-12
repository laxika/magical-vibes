package com.github.laxika.magicalvibes.model.condition;

/**
 * The permanent entered the battlefield by resolving as a spell that was cast.
 *
 * @param byController whether the spell must have been cast by the controller of the ability being
 *                     checked ("if you cast it"), compared with the caster the permanent recorded;
 *                     false accepts a spell cast by any player
 */
public record WasCast(boolean byController) implements Condition, EntryFactCondition {

    public WasCast() {
        this(false);
    }

    @Override
    public String conditionName() {
        return "was cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "it was not cast";
    }
}
