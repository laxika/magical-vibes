package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static effect that keeps damage marked on a set of creatures through cleanup: printed, or gained by
 * the source permanent.
 */
public interface OpponentCreatureDamagePersistenceEffect extends CardEffect {

    /** Whose damage stays (the creatures the opponents control, every creature). */
    PermanentSubject creatures();
}
