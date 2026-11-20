package com.github.laxika.magicalvibes.model.effect;

public record AssignCombatDamageWithManaValueEffect(PermanentSubject subject)
        implements CombatDamageAssignmentEffect {

    @Override
    public CombatDamageAssignmentMode assignmentMode() {
        return CombatDamageAssignmentMode.MANA_VALUE;
    }
}
