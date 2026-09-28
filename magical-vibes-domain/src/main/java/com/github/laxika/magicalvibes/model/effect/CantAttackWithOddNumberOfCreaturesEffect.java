package com.github.laxika.magicalvibes.model.effect;

/** Restricts the controller to declaring an even number of attacking creatures. */
public record CantAttackWithOddNumberOfCreaturesEffect() implements CombatAttackCountRestrictionEffect {

    @Override
    public boolean allowsAttackCount(int attackerCount) {
        return attackerCount % 2 == 0;
    }

    @Override
    public String restrictionViolationMessage() {
        return "You can't attack with an odd number of creatures.";
    }
}
