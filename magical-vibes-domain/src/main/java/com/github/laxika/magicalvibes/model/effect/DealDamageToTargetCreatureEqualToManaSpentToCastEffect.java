package com.github.laxika.magicalvibes.model.effect;

/**
 * Deal damage to target creature equal to the amount of mana spent to cast this spell.
 * Total mana spent is snapshotted into the stack entry's xValue at cast time.
 */
public record DealDamageToTargetCreatureEqualToManaSpentToCastEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
