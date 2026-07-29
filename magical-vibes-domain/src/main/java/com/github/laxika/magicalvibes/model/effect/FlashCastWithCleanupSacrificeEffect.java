package com.github.laxika.magicalvibes.model.effect;

/**
 * The Mirage flash-permanent clause: "You may cast this spell as though it had flash. If you cast
 * it any time a sorcery couldn't have been cast, the controller of the permanent it becomes
 * sacrifices it at the beginning of the next cleanup step."
 *
 * <p>Declared in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC} on the card itself
 * (it is read from the hand, before the permanent exists). Two engine touch points:
 * <ul>
 *   <li>{@code CastingPermissionService.canCastWithTiming} lets the card be cast whenever its
 *       controller has priority;</li>
 *   <li>the cast records whether sorcery timing was available on the stack entry, and the entering
 *       permanent is flagged {@code sacrificeAtNextCleanup} when it was not — the cleanup sweep in
 *       {@code TurnCleanupService} then sacrifices it.</li>
 * </ul>
 * Not a resolvable effect: it has no handler and never appears in an effect list to resolve.
 */
public record FlashCastWithCleanupSacrificeEffect() implements CardEffect {
}
