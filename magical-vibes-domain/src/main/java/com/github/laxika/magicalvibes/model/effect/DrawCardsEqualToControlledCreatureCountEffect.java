package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB/triggered effect that draws cards equal to the number of creatures the controller controls.
 * The count is determined at resolution time.
 * Used for cards like Tishana, Voice of Thunder ("When Tishana enters, draw a card for each creature you control.").
 */
public record DrawCardsEqualToControlledCreatureCountEffect() implements CardEffect {
}
