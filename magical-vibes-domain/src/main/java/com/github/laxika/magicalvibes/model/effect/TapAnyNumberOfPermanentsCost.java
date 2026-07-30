package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * SPELL-slot additional cast cost: "As an additional cost to cast this spell, tap any number of
 * untapped permanents you control matching {@code filter}" (Burn at the Stake — untapped
 * creatures). Any count from zero upwards is a legal payment, so the cost never blocks
 * castability.
 *
 * <p>The number of permanents tapped this way becomes the spell's X value, so a companion effect
 * can scale with it (Burn at the Stake pairs this with damage of
 * {@code Scaled(XValue(), 3)}). The chosen permanents ride on
 * {@code PlayCardRequest.additionalCostSacrificePermanentIds} (the shared multi-permanent cost
 * field) and are validated by {@code AdditionalSpellCostService} / paid by
 * {@code SpellCastingService} before any mana is spent.
 *
 * <p>Contrast {@link TapXPermanentsCost} (activated-ability only, exact announced count) and
 * {@link TapMultiplePermanentsCost} (activated-ability only, fixed count).
 */
public record TapAnyNumberOfPermanentsCost(PermanentPredicate filter) implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
