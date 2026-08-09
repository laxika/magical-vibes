package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a static effect that requires every creature able to block to block each combat.
 * The combat declaration service evaluates this requirement against the current legal attackers
 * and blockers rather than changing a permanent's characteristics.
 */
public interface GlobalMustBlockEachCombatEffect extends CardEffect {
}
