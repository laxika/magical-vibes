package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the creature card that caused the trigger (the stack entry's triggering card), then returns the trigger
 * source to its owner's hand (Diabolic Servitude). The death collector puts the ability on the stack only for the
 * death of the permanent linked to the source.
 */
public record ExileTriggeringCreatureAndReturnSourceToHandEffect() implements CardEffect {
}
