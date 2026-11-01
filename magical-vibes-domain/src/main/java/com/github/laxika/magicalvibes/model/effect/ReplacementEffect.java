package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for replacement effects (MTG rule 614).
 * <p>
 * Replacement effects modify how an event happens rather than triggering after it.
 * They never go on the stack and can't be responded to. Examples include
 * "enters the battlefield tapped", "enters with N counters", and
 * "enters as a copy of".
 * <p>
 * A card's own replacement effects are static abilities ({@code addStaticEffect}); the
 * ones that modify how the permanent enters (CR 614.1c) are applied during the
 * permanent entry process.
 */
public interface ReplacementEffect extends CardEffect {
}
