package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "This turn, whenever an attacking creature deals combat damage to you, it deals that much damage
 * to its controller."
 *
 * <p>The delayed trigger fires separately for each attacking creature that deals combat damage to
 * the spell's controller; each reflected trigger deals that creature's combat damage back to the
 * creature's controller (the attacking player), with the attacking creature as the damage source.
 * Registered by Harsh Justice.
 */
public record RegisterCombatDamageReflectionEffect() implements CardEffect {
}
