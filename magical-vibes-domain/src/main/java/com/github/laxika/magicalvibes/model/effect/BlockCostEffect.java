package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for static effects that impose an additional mana cost to declare the
 * creature carrying them as a blocker of certain attackers (e.g. Hipparion — "can't block
 * creatures with power 3 or greater unless you pay {1}"). Read at declare-blockers time; the
 * returned amount is paid as an additional cost of the block, mirroring how the attack tax is
 * charged when a creature is declared as an attacker.
 *
 * <p>Descriptive only: the returned amount is a fact drawn from the record's components, never a
 * score. The engine owns evaluation — it computes the attacker's effective power and charges the
 * amount itself.
 */
public interface BlockCostEffect extends CardEffect {

    /**
     * Generic mana the controller must pay to block an attacker with the given effective power.
     * {@code 0} means the block is free.
     */
    int blockCost(int attackerPower);
}
