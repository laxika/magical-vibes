package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Capability interface for static effects that impose an additional mana cost to declare the
 * creature carrying them as an attacker (e.g. Phyrexian Marauder — "can't attack unless you pay
 * {1} for each +1/+1 counter on it"). Read at declare-attackers time; the returned amount is paid
 * as an additional cost of the attack, mirroring {@link BlockCostEffect} on the block side.
 *
 * <p>Descriptive only: the returned amount is a fact drawn from the record's components and the
 * creature's live state, never a score. The engine owns evaluation and charges the amount itself.
 */
public interface AttackCostEffect extends CardEffect {

    /**
     * Generic mana the controller must pay to declare {@code creature} as an attacker.
     * {@code 0} means the attack is free.
     */
    int attackCost(Permanent creature);
}
