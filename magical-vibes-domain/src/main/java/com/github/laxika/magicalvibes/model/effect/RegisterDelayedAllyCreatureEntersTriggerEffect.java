package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed triggered ability that resolves {@code effect} whenever a creature the
 * controller controls enters the battlefield for the rest of the turn.
 *
 * @param effect the effect resolved for each creature that enters
 */
public record RegisterDelayedAllyCreatureEntersTriggerEffect(CardEffect effect) implements CardEffect {
}
