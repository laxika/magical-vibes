package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for {@code END_STEP_TRIGGERED} effects that act on the player whose end step
 * it is ("At the beginning of each player's end step, ... that player ...").
 *
 * <p>{@code StepTriggerService} bakes the end-step player (the active player) into the stack
 * entry's {@code targetId} for these effects, so their handlers can read
 * {@code entry.getTargetId()} as that player. Without the marker the generic end-step branch
 * pushes the trigger with a {@code null} target id.
 *
 * <p>Descriptive only, and not a targeting declaration: the end-step player is determined by
 * the trigger, not chosen, so no {@code TargetSpec} is involved.
 */
public interface EndStepPlayerTargetedEffect extends CardEffect {
}
