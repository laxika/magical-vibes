package com.github.laxika.magicalvibes.model.effect;

/** Capability for an effect that restricts the number of creatures declared as attackers. */
public interface CombatAttackCountRestrictionEffect extends CardEffect {

    boolean allowsAttackCount(int attackerCount);

    default String restrictionViolationMessage() {
        return "That number of creatures can't attack.";
    }
}
