package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.UUID;

/**
 * Marker effect carried by the "you may choose new targets for the copy" {@code PendingMayAbility}
 * queued after {@link CopyControllerActivatedAbilityEffect} creates a copy of an activated ability.
 * The retained {@code ability} and {@code sourcePermanentId} let
 * {@code MayCopyHandlerService.handleCopyActivatedAbilityRetargetChoice} recompute the legal targets
 * for the copy on the stack.
 *
 * <p>Used by Rings of Brighthearth.</p>
 *
 * @param ability           the activated ability the copy was made from
 * @param sourcePermanentId the permanent that activated the ability (used to compute legal targets)
 */
public record CopyActivatedAbilityRetargetEffect(
        ActivatedAbility ability,
        UUID sourcePermanentId
) implements CardEffect {
}
