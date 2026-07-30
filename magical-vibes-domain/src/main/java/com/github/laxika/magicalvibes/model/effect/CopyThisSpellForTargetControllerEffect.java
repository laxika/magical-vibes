package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a copy of the spell that is currently resolving, controlled by the controller of the
 * permanent that spell targets, and lets that player choose a new target for the copy.
 *
 * <p>The "chain" half of Chain Stasis — "that creature's controller may pay {2}{U}. If the player
 * does, they may copy this spell and may choose a new target for that copy." Wrap it in a
 * {@link MayPayManaEffect} with {@link MayPayPayer#TARGET_PERMANENT_CONTROLLER} so the payment
 * prompt goes to the same player. Because the copy carries the source's whole effect list, the
 * copy offers the pay-to-copy choice again and the chain continues on its own.</p>
 */
public record CopyThisSpellForTargetControllerEffect() implements CardEffect {
}
