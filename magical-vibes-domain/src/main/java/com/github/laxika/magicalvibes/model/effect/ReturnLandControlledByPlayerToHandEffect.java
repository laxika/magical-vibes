package com.github.laxika.magicalvibes.model.effect;

/**
 * A player returns a land they control to its owner's hand. The acting player is the one carried on
 * the entry's {@code targetId} — "that player" (the caster) for spell-cast triggers like Mana Breach —
 * or, when no target is set, the resolving controller (Kefnet the Mindful's "you may return a land you
 * control", wrapped in a {@code MayEffect} to make the return optional). If that player controls no
 * lands, nothing happens; otherwise they choose which land via the shared {@code BounceCreature} choice
 * context.
 */
public record ReturnLandControlledByPlayerToHandEffect() implements CardEffect {
}
