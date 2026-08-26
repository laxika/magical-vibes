package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * SPELL-slot additional cast cost: "As an additional cost to cast this spell, you may sacrifice any
 * number of permanents you control matching {@code filter}" (Devouring Greed — Spirits). Any count
 * from zero upwards is a legal payment, so the cost never blocks castability.
 *
 * <p>By default, the number of permanents sacrificed this way becomes the spell's X value, so a
 * companion effect can scale with it (Devouring Greed pairs this with a life loss of
 * {@code Sum(Fixed(2), Scaled(XValue(), 2))}). Cards that use the total power of the sacrificed
 * permanents can set {@code trackSacrificedPower} to true; the effective powers are snapshotted
 * before the cost is paid. The chosen permanents ride on
 * {@code PlayCardRequest.additionalCostSacrificePermanentIds} (the shared multi-permanent cost
 * field) and are validated by {@code AdditionalSpellCostService} / paid by
 * {@code SpellCastingService} before any mana is spent.
 *
 * <p>Contrast {@link SacrificeMultiplePermanentsCost} (exact count) and
 * {@link SacrificeXPermanentsCost} (activated-ability only, announced X).
 */
public record SacrificeAnyNumberOfPermanentsCost(PermanentPredicate filter, boolean trackSacrificedPower)
        implements CostEffect {

    public SacrificeAnyNumberOfPermanentsCost(PermanentPredicate filter) {
        this(filter, false);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
