package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile cards from the top of your library until you exile cards with total mana value
 * {@code totalManaValueThreshold} or greater, then you may cast any number of spells from
 * among them without paying their mana costs.
 */
public record ImprovisationCapstoneEffect(int totalManaValueThreshold) implements CardEffect {
}
