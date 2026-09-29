package com.github.laxika.magicalvibes.model.effect;

public record AssignCombatDamageWithManaValueEffect(GrantScope scope)
        implements CombatDamageAssignmentEffect {

    @Override
    public CombatDamageAssignmentMode assignmentMode() {
        return CombatDamageAssignmentMode.MANA_VALUE;
    }
}
