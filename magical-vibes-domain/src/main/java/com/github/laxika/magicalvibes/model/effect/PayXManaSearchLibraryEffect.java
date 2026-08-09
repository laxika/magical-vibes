package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * On resolution, the controller chooses a value for X, pays that much generic mana, and searches
 * their library for one matching card with mana value X or less to put onto the battlefield. The
 * effect owns the optional resolution choice so declining is distinct from choosing X=0, which
 * still performs the search for zero-mana cards.
 */
public record PayXManaSearchLibraryEffect(CardPredicate filter) implements CardEffect {
}
