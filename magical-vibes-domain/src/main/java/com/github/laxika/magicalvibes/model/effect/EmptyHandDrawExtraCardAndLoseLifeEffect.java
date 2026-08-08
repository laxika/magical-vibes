package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement effect: while a source with this effect is on the battlefield, if its
 * controller would draw a card while they have no cards in hand, they draw two cards and lose
 * 1 life instead (Blood Scrivener).
 */
public record EmptyHandDrawExtraCardAndLoseLifeEffect() implements CardEffect {
}
