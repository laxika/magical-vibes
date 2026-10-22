package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose printed mana cost consists only of generic mana symbols totalling at most
 * {@code maxGenericCost}, so a card with mana cost {@code {W}}, an {@code {X}} cost, or a card with no
 * mana cost never matches even when its mana value is small. Compare with
 * {@link CardMaxManaValuePredicate}, which checks mana value rather than the mana cost itself.
 */
public record CardGenericManaCostAtMostPredicate(int maxGenericCost) implements CardPredicate {
}
