package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles cards from the top of their library until they exile a nonland card, then
 * lets the effect controller cast any number of the exiled spells without paying their mana costs.
 * Cards not cast remain in exile.
 */
public record EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect() implements CardEffect {
}
