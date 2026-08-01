package com.github.laxika.magicalvibes.model.filter;

/**
 * A {@link CardPredicate} that matches any card unconditionally.
 * Use for "spells" with no type/subtype restriction (e.g. Helm of Awakening).
 */
public record CardTruePredicate() implements CardPredicate {
}
