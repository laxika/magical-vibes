package com.github.laxika.magicalvibes.model.amount;

/**
 * The total toughness of the cards exiled with the source permanent (exile entries whose source
 * permanent is the source). Cards without a printed toughness contribute 0. Sutured Ghoul.
 */
public record TotalToughnessOfCardsExiledWithSource() implements DynamicAmount {
}
