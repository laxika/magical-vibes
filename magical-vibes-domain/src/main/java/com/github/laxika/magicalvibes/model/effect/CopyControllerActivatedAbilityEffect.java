package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect that creates a single copy of an activated ability for its controller
 * (CR 707.10). The copy is put on the stack above the original; the copy's controller may choose
 * new targets for it.
 *
 * <p>Built by the engine, never by a card: {@code TriggerCollectionService}'s delayed and mana-linked ability copies
 * (the next loyalty, exhaust or {X} ability copied this turn, a chosen planeswalker type's delayed copy, copy-rider
 * mana), {@code CopyControllerAllSpellsAndAbilitiesEffectHandler}, and
 * {@code CopyTriggeringSpellOrAbilityEffectHandler}, which resolves a card's "copy that ability" through it. The
 * snapshot preserves the ability's state (effects, targets, X value, source) at activation time. The {@code ability}
 * reference is retained so the "you may choose new targets" prompt can recompute the legal targets for the copy.
 * A card writes "copy that ability" as {@link CopyTriggeringSpellOrAbilityEffect}.</p>
 *
 * @param abilitySnapshot   snapshot of the activated ability on the stack at trigger time
 * @param ability           the activated ability that was activated (for retargeting the copy)
 * @param activatingPlayerId the player who activated the ability (and controls the copy)
 */
public record CopyControllerActivatedAbilityEffect(
        StackEntry abilitySnapshot,
        ActivatedAbility ability,
        UUID activatingPlayerId
) implements CardEffect {
}
