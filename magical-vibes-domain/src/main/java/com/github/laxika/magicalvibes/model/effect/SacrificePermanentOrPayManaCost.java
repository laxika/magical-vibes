package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: sacrifice a permanent matching {@code filter} or pay
 * {@code manaCost}. Exactly one option is paid: a controlled permanent selected through
 * {@code PlayCardRequest.sacrificePermanentId}, or the listed mana on top of the spell's normal
 * mana cost.
 */
public record SacrificePermanentOrPayManaCost(
        String manaCost, PermanentPredicate filter, String description) implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return filter instanceof PermanentIsCreaturePredicate;
    }
}
