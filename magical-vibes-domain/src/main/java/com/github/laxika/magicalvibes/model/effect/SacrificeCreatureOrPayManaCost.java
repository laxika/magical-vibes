package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Backward-compatible creature-specific form of {@link SacrificePermanentOrPayManaCost}.
 * New cards with a filtered permanent sacrifice should use the generic form.
 */
public record SacrificeCreatureOrPayManaCost(String manaCost) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return true;
    }
}
