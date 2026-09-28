package com.github.laxika.magicalvibes.model.effect;

/**
 * Queues the reflexive ability of Jailbreak after the targeted graveyard return succeeds.
 *
 * <p>The returned permanent's mana value is read when this effect resolves and becomes the
 * upper bound for the follow-up graveyard target. The follow-up itself is targeted only after the
 * first return has completed.</p>
 */
public record ReturnedPermanentReturnsTargetPermanentCardFromGraveyardEffect() implements CardEffect {
}
