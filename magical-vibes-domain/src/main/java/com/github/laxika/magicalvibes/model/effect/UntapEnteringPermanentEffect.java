package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-materialising marker for "whenever a permanent you control enters tapped, untap it."
 *
 * <p>The enter-trigger collector resolves the entering permanent and puts an
 * {@link UntapPermanentsEffect} onto the stack with that permanent recorded as a non-targeting
 * reference. This is not a target: the permanent is fixed by the event that caused the trigger.
 */
public record UntapEnteringPermanentEffect() implements CardEffect {
}
