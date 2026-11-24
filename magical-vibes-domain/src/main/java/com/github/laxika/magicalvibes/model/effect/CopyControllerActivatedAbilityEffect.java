package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.UUID;

/**
 * Resolution-time effect that creates a single copy of an activated or triggered ability, as it was captured in
 * {@code abilitySnapshot}, for {@code activatingPlayerId} (CR 707.10). The copy is put on the stack above the original
 * with every decision made for it (targets, X, the objects used to pay its costs, the chosen permanent); its controller
 * may choose new targets for it (CR 707.10c).
 *
 * <p>Built by the engine, never by a card: the copies the mana that copies the ability it is spent on makes
 * ({@code SpellTriggers}), {@code CopyControllerAllSpellsAndAbilitiesEffectHandler} ("copy all other activated and
 * triggered abilities you control"), and {@code CopyTriggeringSpellOrAbilityEffectHandler}, which resolves a card's
 * "copy that ability" through it, of a triggered ability too, even after it has left the stack. A card writes "copy
 * that ability" as {@link CopyTriggeringSpellOrAbilityEffect}.</p>
 *
 * @param abilitySnapshot    snapshot of the ability as it was on the stack
 * @param ability            the activated ability that was activated (for retargeting the copy), or {@code null}
 * @param activatingPlayerId the player who controls the copy
 */
public record CopyControllerActivatedAbilityEffect(
        StackEntry abilitySnapshot,
        ActivatedAbility ability,
        UUID activatingPlayerId
) implements CardEffect {
}
