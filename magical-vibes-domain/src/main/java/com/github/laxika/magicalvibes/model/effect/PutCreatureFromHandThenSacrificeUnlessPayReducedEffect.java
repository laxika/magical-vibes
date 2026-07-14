package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may put a creature card from your hand onto the battlefield. If you do, sacrifice it unless
 * you pay its mana cost reduced by {genericReduction}." (e.g. Flash, reduction 2)
 *
 * <p>Reuses the hand-to-battlefield card choice ({@link PutCardToBattlefieldEffect}'s machinery). The
 * chosen creature enters normally; then a resolution-time pay-or-sacrifice prompt — a
 * {@link ForcedCostOrElseEffect} with {@link PayManaCost} (the entering creature's mana cost reduced by
 * {@code genericReduction}) and a {@link SacrificeSelfEffect} penalty — charges the reduced cost or
 * sacrifices the creature.
 *
 * @param genericReduction generic mana by which the entering creature's mana cost is reduced
 */
public record PutCreatureFromHandThenSacrificeUnlessPayReducedEffect(int genericReduction) implements CardEffect {
}
