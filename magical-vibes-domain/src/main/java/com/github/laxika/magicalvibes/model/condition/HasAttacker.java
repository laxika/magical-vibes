package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One or more of the controller's attacking creatures match the predicate. In an attack trigger it is a fact about the
 * attack as declared ({@link DeclaredAttackCondition}): "whenever you attack with your commander", "if a Pirate and a
 * Vehicle attacked this combat".
 */
public record HasAttacker(PermanentPredicate predicate) implements Condition, DeclaredAttackCondition {

    @Override
    public String conditionName() {
        return "matching attacker";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching attacker";
    }
}
