package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an effect that filters the target-choice trigger event before its wrapped effect is
 * put onto the stack.
 */
public interface TargetChoiceTriggerEffect extends CardEffect {

    CardEffect wrapped();
}
