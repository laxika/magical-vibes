package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an {@code Triggers.endStep(ActivePlayer.ANY)} effect that triggers only during the monarch's end
 * step. The active player is still captured as the non-targeting end-step player.
 */
public interface MonarchEndStepTriggeredEffect extends CardEffect {
}
