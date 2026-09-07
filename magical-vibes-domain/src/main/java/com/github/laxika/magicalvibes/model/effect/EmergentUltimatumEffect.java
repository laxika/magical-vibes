package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for up to three differently named monocolored cards, exiles
 * the chosen cards, and gives an opponent one choice to shuffle back before the rest may be cast
 * without paying their mana costs.
 */
public record EmergentUltimatumEffect() implements CardEffect {
}
