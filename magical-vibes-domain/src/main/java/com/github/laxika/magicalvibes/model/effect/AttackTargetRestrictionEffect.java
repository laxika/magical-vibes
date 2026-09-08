package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static effect that removes a permanent from the legal set of combat attack
 * targets. Combat legality evaluates this capability on the potential target permanent.
 */
public interface AttackTargetRestrictionEffect extends CardEffect {

    boolean preventsBeingAttacked();
}
