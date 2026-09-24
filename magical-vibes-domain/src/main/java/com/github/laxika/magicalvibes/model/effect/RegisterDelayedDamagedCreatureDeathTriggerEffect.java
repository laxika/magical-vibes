package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger for the rest of the turn: whenever a creature the registering player
 * controls that was dealt damage by this effect's source dies, the wrapped effect resolves.
 *
 * @param effect the effect resolved for each qualifying creature death
 */
public record RegisterDelayedDamagedCreatureDeathTriggerEffect(CardEffect effect) implements CardEffect {
}
