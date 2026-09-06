package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a copy of the spell that is currently resolving, controlled by the targeted player or
 * the controller of the permanent that spell targets, and lets that player choose a new target
 * for the copy.
 *
 * <p>The "chain" half of Chain Stasis and Chain Lightning — the targeted permanent's controller
 * or player may pay, then copy this spell. Wrap it in a {@link MayPayManaEffect} with the matching
 * target-based payer so the payment prompt goes to the same player. Because the copy carries the
 * source's whole effect list, the copy offers the pay-to-copy choice again and the chain continues
 * on its own.</p>
 */
public record CopyThisSpellForTargetControllerEffect() implements CardEffect {
}
