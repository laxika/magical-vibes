package com.github.laxika.magicalvibes.model.effect;

/**
 * Library of Lat-Nam. An opponent chooses one — either you draw three cards at the beginning of the
 * next turn's upkeep, or you search your library for a card, put that card into your hand, then
 * shuffle. The choosing opponent is prompted through the may-ability (accept/decline) system: accept
 * schedules the delayed draw, decline begins the library search.
 */
public record LibraryOfLatNamEffect() implements CardEffect {
}
