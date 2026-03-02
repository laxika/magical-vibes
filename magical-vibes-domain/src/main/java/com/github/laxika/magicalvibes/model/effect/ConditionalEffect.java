package com.github.laxika.magicalvibes.model.effect;

/**
 * A conditional wrapper around a {@link CardEffect} that only applies when a
 * game-state condition is met (e.g. metalcraft, equipped).
 * <p>
 * Implementations provide the wrapped effect and a human-readable condition name
 * for logging. Condition evaluation is handled externally by the engine since
 * domain records cannot depend on game services.
 */
public interface ConditionalEffect extends CardEffect {

    /** The inner effect to resolve when the condition is met. */
    CardEffect wrapped();

    /** Human-readable condition name for log messages (e.g. "metalcraft", "equipped"). */
    String conditionName();

    /** Human-readable reason shown when the condition is not met (e.g. "fewer than three artifacts"). */
    String conditionNotMetReason();
}
