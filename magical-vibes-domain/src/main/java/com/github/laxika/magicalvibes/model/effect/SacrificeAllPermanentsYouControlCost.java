package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost: sacrifice all permanents you control (Kaervek's Spite). Legal with
 * zero permanents. Paid at cast time by {@code SpellCastingService}; stripped from the stack
 * entry by {@code AdditionalSpellCostService}.
 */
public record SacrificeAllPermanentsYouControlCost() implements CostEffect {
}
