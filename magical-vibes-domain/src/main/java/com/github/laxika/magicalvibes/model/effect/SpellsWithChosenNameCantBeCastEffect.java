package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can cast spells with the same name as the card name
 * chosen by the source permanent (tracked via permanent.chosenName).
 * Used by Nevermore.
 */
public record SpellsWithChosenNameCantBeCastEffect() implements CardEffect {
}
