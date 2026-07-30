package com.github.laxika.magicalvibes.model.effect;

/**
 * Produces one mana of any color (player chooses) that can only be spent to cast
 * a creature spell of the source permanent's chosen creature type.
 * Used by Pillar of Origins, Unclaimed Territory, etc.
 *
 * <p>When {@code makesSpellUncounterable} is set, a spell paid for with this mana also can't be
 * countered (Cavern of Souls). The mana is tracked as an uncounterable-granting subset of the
 * pool's subtype-creature bucket; spending any of it marks the spell uncounterable.
 */
public record AwardAnyColorChosenSubtypeCreatureManaEffect(boolean makesSpellUncounterable) implements ManaProducingEffect {

    public AwardAnyColorChosenSubtypeCreatureManaEffect() {
        this(false);
    }

    @Override
    public int estimatedWildcardMana() {
        return 1;
    }
}
