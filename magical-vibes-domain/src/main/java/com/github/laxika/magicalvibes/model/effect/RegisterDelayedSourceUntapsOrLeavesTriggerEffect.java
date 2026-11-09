package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this permanent leaves the battlefield or becomes untapped, [triggerEffect] on the creature": creates a
 * {@code DelayedSourceUntapsOrLeavesTrigger} for the resolving ability's target (Giant Oyster). Declares no target of
 * its own; the ability's other effect declares it. {@code triggerEffect} resolves on a non-targeting entry aimed at
 * that creature.
 */
public record RegisterDelayedSourceUntapsOrLeavesTriggerEffect(CardEffect triggerEffect) implements CardEffect {
}
