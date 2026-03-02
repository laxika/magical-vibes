package com.github.laxika.magicalvibes.model.effect;

/**
 * A conditional wrapper that selects between two effects based on a game-state
 * condition (e.g. metalcraft). When the condition is met the upgraded effect
 * is resolved; otherwise the base effect is used.
 * <p>
 * Condition evaluation is handled externally by the engine since domain records
 * cannot depend on game services.
 */
public interface ReplacementConditionalEffect extends CardEffect {

    /** The default effect used when the condition is not met. */
    CardEffect baseEffect();

    /** The upgraded effect used when the condition is met. */
    CardEffect upgradedEffect();

    /** Human-readable condition name for log messages (e.g. "metalcraft"). */
    String conditionName();
}
