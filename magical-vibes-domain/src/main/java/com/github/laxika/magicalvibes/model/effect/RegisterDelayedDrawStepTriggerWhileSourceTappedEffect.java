package com.github.laxika.magicalvibes.model.effect;

/**
 * "For as long as this permanent remains tapped, ... at the beginning of each of your draw steps, [triggerEffect] on
 * that creature": creates a {@code DelayedDrawStepTriggerWhileSourceTapped} for the resolving ability's target
 * (Giant Oyster). Declares no target of its own; the ability's other effect declares it. Nothing is created when the
 * source has untapped or left the battlefield since the ability was activated, because the duration has already
 * ended. {@code triggerEffect} resolves on a non-targeting entry aimed at that creature.
 */
public record RegisterDelayedDrawStepTriggerWhileSourceTappedEffect(CardEffect triggerEffect) implements CardEffect {
}
