package com.github.laxika.magicalvibes.model.condition;

/**
 * Intervening-if for "each opponent's end step" triggers: the end-step player (baked into the
 * stack entry's {@code targetId}) didn't cast a creature spell this turn. A countered creature
 * spell still counts as cast. Used by Predatory Advantage.
 */
public record EndStepPlayerDidntCastCreatureSpell() implements Condition {

    @Override
    public String conditionName() {
        return "didn't cast a creature spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player cast a creature spell this turn";
    }
}
