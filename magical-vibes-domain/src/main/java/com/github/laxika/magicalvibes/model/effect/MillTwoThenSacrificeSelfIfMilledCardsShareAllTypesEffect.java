package com.github.laxika.magicalvibes.model.effect;

/** Mills two cards and sacrifices the source if any two milled cards have the exact same card types. */
public record MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect() implements CardEffect {
}
