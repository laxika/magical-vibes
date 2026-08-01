package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * SPELL-slot additional cast cost: "As an additional cost to cast this spell, return any number of
 * permanents you control matching {@code filter} to their owner's hand" (Infernal Harvest —
 * Swamps). Any count from zero upwards is a legal payment, so the cost never blocks castability.
 *
 * <p>The number of permanents returned this way becomes the spell's X value, so a companion effect
 * can scale with it (Infernal Harvest pairs this with
 * {@code DealDividedDamageEffect.xAmongTargetCreatures()}). The chosen permanents ride on
 * {@code PlayCardRequest.additionalCostSacrificePermanentIds} (the shared multi-permanent cost
 * field) and are validated by {@code AdditionalSpellCostService} / paid by
 * {@code SpellCastingService} before any mana is spent.
 *
 * <p>Contrast {@link ReturnCreatureToHandCost} (exactly one creature) and
 * {@link ReturnMultiplePermanentsToHandCost} (activated-ability only, fixed count).
 */
public record ReturnAnyNumberOfPermanentsToHandCost(PermanentPredicate filter) implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
