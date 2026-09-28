package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for Nykthos Paragon's life-gain ability. The trigger collector locks the life
 * gained by the event into the mass counter placement and queues the optional ability; the
 * surrounding once-per-turn trigger marks the source only when the counter placement is accepted.
 */
public record NykthosParagonLifeGainEffect() implements CardEffect {
}
