package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the target player lose life equal to the triggering spell's mana value minus the mana
 * spent to cast it, both read as the ability resolves ({@code TargetSpellManaValue}: the spell, else as it last existed
 * on the stack; {@code ManaSpentToCast}: what the spell recorded as it became cast, from the entry's snapshot of it).
 */
public record LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
