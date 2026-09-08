package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell effect: choose a card name; until the controller's next turn, spells with that name can't
 * be cast and lands with that name can't be played.
 */
public record ChooseCardNameSpellsAndLandsCantBePlayedUntilNextTurnEffect() implements CardEffect {
}
