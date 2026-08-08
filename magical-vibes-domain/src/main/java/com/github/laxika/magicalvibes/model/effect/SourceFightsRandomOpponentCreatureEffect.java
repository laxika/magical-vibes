package com.github.laxika.magicalvibes.model.effect;

/**
 * The source permanent fights a creature an opponent controls chosen uniformly at random as the
 * effect resolves (CR 701.14a). Nothing is targeted, so no target is declared when the ability is
 * put onto the stack and shroud/hexproof do not shrink the pool — the same modelling
 * {@link DealDamageToRandomAnyTargetEffect} uses for "chosen at random" recipients.
 *
 * <p>Used by Scab-Clan Giant ("When this creature enters, it fights target creature an opponent
 * controls chosen at random.").
 */
public record SourceFightsRandomOpponentCreatureEffect() implements CardEffect {
}
