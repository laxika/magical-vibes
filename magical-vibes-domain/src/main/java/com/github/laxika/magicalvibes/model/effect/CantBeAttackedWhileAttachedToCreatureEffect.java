package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for a permanent that can't be attacked while it is attached to a creature.
 */
public record CantBeAttackedWhileAttachedToCreatureEffect() implements AttackTargetRestrictionEffect {

    @Override
    public boolean preventsBeingAttacked() {
        return true;
    }
}
