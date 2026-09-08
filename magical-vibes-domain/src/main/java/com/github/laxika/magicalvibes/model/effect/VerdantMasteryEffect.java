package com.github.laxika.magicalvibes.model.effect;

/** Searches for up to four basic lands and distributes the found cards for Verdant Mastery. */
public record VerdantMasteryEffect(boolean alternateCost) implements CardEffect {
}
