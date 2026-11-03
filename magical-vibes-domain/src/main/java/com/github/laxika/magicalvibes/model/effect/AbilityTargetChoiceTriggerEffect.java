package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that react when a player or permanent becomes the target of an
 * activated or triggered ability controlled by this permanent's controller. Don't author it: the
 * trigger {@code Triggers.playerOrPermanentBecomesTargetOfYourAbility()} says this, and its legacy
 * view ({@code TriggeredAbility.legacyEffects()}) gives it to the target-choice collector that still
 * judges it.
 */
public record AbilityTargetChoiceTriggerEffect(CardEffect wrapped) implements TargetChoiceTriggerEffect {
}
