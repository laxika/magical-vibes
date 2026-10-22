package com.github.laxika.magicalvibes.model.condition;

/**
 * Logical negation of another condition — met exactly when {@code inner} is not met
 * (e.g. Hotheaded Giant's "unless you've cast another red spell this turn"). Evaluation
 * recurses into {@code inner} with the same {@code ConditionContext}.
 */
public record NotCondition(Condition inner) implements Condition {

    @Override
    public String conditionName() {
        return "not " + inner.conditionName();
    }

    @Override
    public String conditionNotMetReason() {
        return inner.conditionName();
    }

    /** "If you control no …" is checked as the trigger event occurs exactly when its positive form is. */
    @Override
    public boolean isEtbTriggerGate() {
        return inner.isEtbTriggerGate();
    }
}
