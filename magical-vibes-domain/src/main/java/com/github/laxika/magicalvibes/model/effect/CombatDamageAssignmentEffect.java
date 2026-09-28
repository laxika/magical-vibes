package com.github.laxika.magicalvibes.model.effect;

public interface CombatDamageAssignmentEffect extends CardEffect {

    GrantScope scope();

    CombatDamageAssignmentMode assignmentMode();
}
