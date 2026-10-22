package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "The name {this} can't be chosen." While the source is on the battlefield with
 * its abilities, no player can choose the source's name as a card name (Glimpse, the Unthinkable).
 */
public record SourceNameCantBeChosenEffect() implements CardEffect {
}
