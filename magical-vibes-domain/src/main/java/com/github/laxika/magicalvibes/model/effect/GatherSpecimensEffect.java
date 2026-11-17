package com.github.laxika.magicalvibes.model.effect;

/**
 * "If a creature would enter the battlefield under an opponent's control this turn, it enters under
 * your control instead." (Gather Specimens)
 * Adds a floating effect until end of turn for the controller. A creature that would
 * enter under any opponent's control instead enters under the gatherer's control (control-changing
 * replacement effect applied in {@code BattlefieldEntryService}). Cleared at turn cleanup.
 */
public record GatherSpecimensEffect() implements CardEffect {
}
