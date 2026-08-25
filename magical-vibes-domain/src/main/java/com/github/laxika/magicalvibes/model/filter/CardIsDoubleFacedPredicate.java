package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards represented by a physical double-faced card in the engine.
 *
 * <p>Split, flip, aftermath, and meld cards also have a second modeled face, but are not
 * double-faced cards for card-text restrictions, so they are excluded here.</p>
 */
public record CardIsDoubleFacedPredicate() implements CardPredicate {
}
