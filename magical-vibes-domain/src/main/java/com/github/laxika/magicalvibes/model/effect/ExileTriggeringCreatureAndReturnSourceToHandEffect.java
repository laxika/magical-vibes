package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the creature card that caused the trigger (the stack entry's triggering card), then returns the trigger
 * source to its owner's hand (Diabolic Servitude). The card is exiled only while it is still the graveyard card the
 * death was about (CR 400.7). Diabolic Servitude's trigger, {@code Triggers.creatureDies} filtered by
 * {@code PermanentIsChosenPermanentPredicate}, makes its matcher put the ability on the stack only for the death of
 * the creature the source returned.
 */
public record ExileTriggeringCreatureAndReturnSourceToHandEffect() implements CardEffect {
}
