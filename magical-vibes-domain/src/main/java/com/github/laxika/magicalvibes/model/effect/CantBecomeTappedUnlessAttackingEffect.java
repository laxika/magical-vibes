package com.github.laxika.magicalvibes.model.effect;

/** Prevents the targeted permanent from becoming tapped unless it is being declared as an attacker. */
public record CantBecomeTappedUnlessAttackingEffect() implements TapRestrictionEffect {

    @Override
    public boolean preventsTappingUnlessAttacking() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
