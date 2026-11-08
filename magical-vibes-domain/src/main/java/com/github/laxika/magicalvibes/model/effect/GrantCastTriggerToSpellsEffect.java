package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability: the controller's spells of kind {@code grant} have a triggered keyword ability that triggers
 * when the spell is cast, such as cascade (CR 702.85a), storm (CR 702.40a) or demonstrate (CR 702.144a).
 * {@code ability} is what that trigger resolves: a {@link CascadeEffect}, storm's
 * {@link CopyTriggeringSpellEffect#storm()} (put on the stack with the cast spell as its triggering spell), or a
 * {@link MayEffect} wrapping a {@link DemonstrateEffect}.
 *
 * <p>Read by {@code TriggerCollectionService.checkSpellCastTriggers}, which puts one trigger on the stack for
 * each permanent with a matching grant.
 */
public record GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant grant, CardEffect ability)
        implements CardEffect {
}
