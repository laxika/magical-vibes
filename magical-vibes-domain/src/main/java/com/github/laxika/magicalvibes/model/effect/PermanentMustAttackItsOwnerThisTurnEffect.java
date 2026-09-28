package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the current target creature attack its owner this turn if able.
 *
 * <p>This is used as a synchronous rider for effects that temporarily seize a permanent. The
 * permanent's owner is read when the rider resolves, while the requirement itself lasts until
 * turn cleanup.</p>
 */
public record PermanentMustAttackItsOwnerThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
