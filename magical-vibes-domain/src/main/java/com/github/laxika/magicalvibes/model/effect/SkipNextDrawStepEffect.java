package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the resolving controller skip their next draw step ("you skip your next draw step" —
 * Ivory Gargoyle). Modelled by incrementing a per-player counter
 * ({@code GameData.skipNextDrawStepCount}) which the turn engine reads at the beginning of that
 * player's draw step, decrementing it as each draw step is skipped.
 *
 * <p>This is the one-shot sibling of the static {@link SkipDrawStepEffect} marker (Colfenor's
 * Plans), which applies for as long as its source is on the battlefield. Like that marker, the
 * whole step is skipped: no turn-based draw and no draw-step triggered abilities.
 */
public record SkipNextDrawStepEffect() implements CardEffect {
}
