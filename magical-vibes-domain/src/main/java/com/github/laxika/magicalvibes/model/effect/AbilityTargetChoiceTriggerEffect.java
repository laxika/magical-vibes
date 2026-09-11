package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that react when a player or permanent becomes the target of an
 * activated or triggered ability controlled by this permanent's controller.
 */
public record AbilityTargetChoiceTriggerEffect(CardEffect wrapped) implements TargetChoiceTriggerEffect {
}
