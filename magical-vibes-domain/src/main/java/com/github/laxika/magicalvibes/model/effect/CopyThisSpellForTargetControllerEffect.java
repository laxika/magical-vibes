package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a copy of the spell that is currently resolving, controlled by the targeted player or
 * the controller of the permanent that spell targets, and lets that player choose a new target
 * for the copy.
 *
 * <p>Also "that player or that permanent's controller may discard a card; if the player does, they may copy this
 * spell" and "that player may copy this spell" (Chain of Plasma, Chain of Smog).
 *
 * <p>The "chain" half of Chain Stasis and Chain Lightning — the targeted permanent's controller
 * or player may pay, then copy this spell: {@code UnlessPaysEffect.mayPay(payer, "{R}{R}", new MayEffect(copy, prompt,
 * null, MayChoicePlayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER))}, the "you may copy" asked of the same player the payer
 * subject names. Because the copy carries the
 * source's whole effect list, the copy offers the pay-to-copy choice again and the chain continues
 * on its own.</p>
 */
public record CopyThisSpellForTargetControllerEffect() implements CardEffect {
}
