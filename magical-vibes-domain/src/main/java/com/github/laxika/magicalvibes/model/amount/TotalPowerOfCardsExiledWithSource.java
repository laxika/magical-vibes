package com.github.laxika.magicalvibes.model.amount;

/**
 * The total power of the cards exiled with the source permanent (exile entries whose source
 * permanent is the source). Cards without a printed power (noncreature cards, star-power
 * cards) contribute 0. Sutured Ghoul.
 */
public record TotalPowerOfCardsExiledWithSource() implements DynamicAmount {
}
