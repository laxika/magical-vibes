package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability (CR 603.7a): "When this creature becomes untapped
 * or you lose control of this creature, exile that creature" (Coffin Queen), which also fires when the
 * source leaves the battlefield. "That creature" is the permanent the same ability's earlier
 * {@link ReturnCardFromGraveyardEffect} linked to the source via {@code linkToSource}; the link is
 * consumed here, so each resolution registers its own trigger for its own creature. Nothing is
 * registered if the source has left the battlefield before the ability resolves.
 */
public record RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffect() implements CardEffect {
}
