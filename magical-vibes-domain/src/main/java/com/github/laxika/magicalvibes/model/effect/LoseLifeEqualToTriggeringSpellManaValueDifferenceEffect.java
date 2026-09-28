package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the target player lose life equal to the triggering spell's mana value minus the mana
 * spent to cast it. The spell's mana value is snapshotted when the spell-cast trigger is created.
 */
public record LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect()
        implements TriggeringSpellManaValueEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
