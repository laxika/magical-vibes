package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability that lets its controller optionally look at additional cards each time they
 * surveil. Used by Enhanced Surveillance.
 *
 * @param amount the number of additional cards the controller may look at
 */
public record AdditionalSurveilCardsEffect(int amount) implements CardEffect {
}
