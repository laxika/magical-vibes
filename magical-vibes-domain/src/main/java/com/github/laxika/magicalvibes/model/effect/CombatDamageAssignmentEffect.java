package com.github.laxika.magicalvibes.model.effect;

public interface CombatDamageAssignmentEffect extends CardEffect {

    PermanentSubject subject();

    CombatDamageAssignmentMode assignmentMode();
}
