package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect carried by the "you may choose new targets for the copy" {@code PendingMayAbility}
 * queued after a targeted ability-copy effect creates a copy of an ability.
 * {@code MayCopyHandlerService.handleCopyTriggeredAbilityRetargetChoice} recomputes legal targets
 * from the copy's snapshotted effects on the stack.
 *
 * <p>Used by Strionic Resonator and Peter Parker's Camera.</p>
 */
public record CopyTriggeredAbilityRetargetEffect() implements CardEffect {
}
