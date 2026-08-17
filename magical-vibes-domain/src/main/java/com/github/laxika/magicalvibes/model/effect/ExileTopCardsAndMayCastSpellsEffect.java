package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of your library, then lets you cast any number of the
 * exiled spells without paying their mana costs. The cast choices are made during resolution;
 * uncast cards remain exiled.
 */
public record ExileTopCardsAndMayCastSpellsEffect(int count) implements CardEffect {
}
