package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card carrying a color chosen by the source permanent as it entered the battlefield: any of them, when it
 * chose more than one ("if it's at least one of the chosen colors", Tablet of the Guilds). Source-dependent: matches
 * nothing when the source is gone or made no choice.
 */
public record CardHasSourceChosenColorPredicate() implements CardPredicate {
}
