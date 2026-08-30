package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect restricting whether this creature may attack or block alone.
 *
 * @param restrictsBlocking whether the blocking half of the restriction applies
 * @param restrictsAttacking whether the attacking half of the restriction applies
 */
public record CantAttackOrBlockAloneEffect(boolean restrictsBlocking, boolean restrictsAttacking)
        implements CardEffect {

    public CantAttackOrBlockAloneEffect() {
        this(true, true);
    }

    public CantAttackOrBlockAloneEffect(boolean restrictsBlocking) {
        this(restrictsBlocking, true);
    }
}
