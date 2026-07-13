package com.github.laxika.magicalvibes.model.effect;

/**
 * The player carried on the trigger's {@code targetId} returns a land they control to
 * its owner's hand. Used by spell-cast triggers where "that player" (the caster) bounces
 * a land — e.g. Mana Breach. If that player controls no lands, nothing happens; otherwise
 * they choose which land via the shared {@code BounceCreature} choice context.
 */
public record ReturnLandControlledByPlayerToHandEffect() implements CardEffect {
}
