package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: the permanent this is granted to can't be enchanted by other Auras (CR 702.5 style
 * restriction, e.g. Anti-Magic Aura). Read at Aura-spell targeting time by
 * {@code GameQueryService.cantBeEnchantedByOtherAuras}; never resolved on the stack. Only ever used
 * wrapped in a {@link GrantEffectEffect} (typically scoped to {@code ENCHANTED_CREATURE}).
 */
public record CantBeEnchantedByOtherAurasEffect() implements CardEffect {
}
