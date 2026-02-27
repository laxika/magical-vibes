package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher effect: target player loses N life unless they discard a card.
 * Used by Painful Quandary and similar "lose life or discard" cards.
 * The affected player chooses whether to discard or take the life loss.
 */
public record LoseLifeUnlessDiscardEffect(int lifeLoss) implements CardEffect {
}
