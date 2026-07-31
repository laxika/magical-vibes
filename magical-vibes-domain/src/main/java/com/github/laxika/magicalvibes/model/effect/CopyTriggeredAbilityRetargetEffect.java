package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect carried by the "you may choose new targets for the copy" {@code PendingMayAbility}
 * queued after {@link CopyTargetTriggeredAbilityEffect} creates a copy of a triggered ability.
 * {@code MayCopyHandlerService.handleCopyTriggeredAbilityRetargetChoice} recomputes legal targets
 * from the copy's snapshotted effects on the stack.
 *
 * <p>Used by Strionic Resonator.</p>
 */
public record CopyTriggeredAbilityRetargetEffect() implements CardEffect {
}
