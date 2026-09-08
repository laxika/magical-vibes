package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the player whose spell caused the trigger and offers a nonland card to the
 * source controller to cast without paying its mana cost.
 */
public record ExileTopCardOfTriggeringPlayerLibraryAndMayCastFreeEffect() implements CardEffect {
}
