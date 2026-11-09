package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the target player lose life equal to the triggering spell's mana value minus the mana
 * spent to cast it. The spell's mana value is read as the ability resolves ({@code TargetSpellManaValue}: the spell,
 * else as it last existed on the stack); the mana spent, which the game keeps only while the spell is being cast, is
 * recorded as the ability's X when it triggers.
 */
public record LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
