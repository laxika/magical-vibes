package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power of the creature chosen for a creature-or-revealed-card cast cost, or the
 * revealed card's power carried in the stack entry when the hand-card option was chosen.
 */
public record ChosenCreatureOrRevealedCardPower() implements DynamicAmount {
}
