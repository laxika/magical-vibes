package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice all [permanents matching {@code filter}] you control" as a cost: of an activated ability (Tomb of
 * Urami's lands) or as an additional cost to cast a spell (Soulblast's creatures, Kaervek's Spite's permanents).
 * The payment is automatic because there is no choice about which matching permanents are sacrificed; zero
 * matching permanents is a legal payment, and a matching permanent that can't be sacrificed makes the cost
 * unpayable (CR 118.3). The permanents leave together.
 *
 * @param filter               which of the payer's permanents are sacrificed
 * @param trackSacrificedPower on a spell, whether the sacrificed permanents' total power (at least 0) becomes the
 *                             spell's X ("equal to the total power of the sacrificed creatures")
 */
public record SacrificeAllMatchingPermanentsCost(PermanentPredicate filter, boolean trackSacrificedPower)
        implements CostEffect {

    public SacrificeAllMatchingPermanentsCost(PermanentPredicate filter) {
        this(filter, false);
    }
}
