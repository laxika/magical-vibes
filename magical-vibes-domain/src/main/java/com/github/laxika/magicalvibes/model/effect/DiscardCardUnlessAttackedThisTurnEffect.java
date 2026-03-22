package com.github.laxika.magicalvibes.model.effect;

/**
 * "Discard a card unless you attacked this turn."
 * <p>
 * If the controller declared at least one attacker this turn, the discard is skipped.
 * Otherwise, the controller must discard a card.
 */
public record DiscardCardUnlessAttackedThisTurnEffect() implements CardEffect {
}
