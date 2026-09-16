package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks an effect in the {@code EACH_DRAW_TRIGGERED} slot as applying only during an opponent's
 * draw step. The step trigger collector skips the trigger entirely when the draw-step player is
 * the source's controller, so nothing is put onto the stack on the controller's own draw step.
 */
public interface OpponentDrawStepOnlyEffect extends CardEffect {

    boolean opponentDrawStepOnly();
}
