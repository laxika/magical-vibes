package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A static restriction that prevents every player from playing lands while a battlefield-wide
 * permanent count condition is met.
 */
public interface GlobalLandPlayRestrictionEffect extends CardEffect {

    int minimumCount();

    PermanentPredicate filter();
}
