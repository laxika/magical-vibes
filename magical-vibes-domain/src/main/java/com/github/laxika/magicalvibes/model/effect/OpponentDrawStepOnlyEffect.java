package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for effects placed in the {@code EACH_DRAW_TRIGGERED} slot whose ability reads
 * "at the beginning of each opponent's draw step" rather than "each player's draw step".
 * The step trigger collector skips the trigger entirely when the draw-step player is the
 * source's controller, so nothing is put onto the stack on the controller's own draw step.
 */
public interface OpponentDrawStepOnlyEffect extends CardEffect {
}
