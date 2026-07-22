package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: "sacrifice a creature or pay {manaCost}" (e.g. Eaten Alive's
 * "{3}{B}"). Exactly one option is paid — either a controlled creature via
 * {@code PlayCardRequest.sacrificePermanentId}, or the listed mana on top of the spell's
 * normal mana cost. Satisfiable with a creature or with enough mana for the combined cost.
 */
public record SacrificeCreatureOrPayManaCost(String manaCost) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }

    @Override
    public boolean sacrificesChosenCreature() {
        // AI prefers sacrificing when a creature is available; mana path is used when none is.
        return true;
    }
}
