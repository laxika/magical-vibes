package com.github.laxika.magicalvibes.model.effect;

/**
 * Allows a creature to divide its combat damage among the defending player and any number of
 * creatures that player controls.
 */
public record AssignCombatDamageAmongDefendingPlayerAndCreaturesEffect()
        implements CombatDamageAssignmentToDefendingPlayerAndCreaturesEffect {
}
