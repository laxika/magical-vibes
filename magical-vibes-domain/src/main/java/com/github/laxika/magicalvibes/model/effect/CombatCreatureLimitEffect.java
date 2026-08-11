package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static effect that limits the number of creatures declared as attackers or
 * blockers in one combat.
 */
public interface CombatCreatureLimitEffect extends CardEffect {

    /** Maximum number of creatures that may be declared as attackers. */
    int maxAttackers();

    /** Maximum number of distinct creatures that may be declared as blockers. */
    int maxBlockers();
}
