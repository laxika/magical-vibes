package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura restriction: the enchanted creature can't transform. Consulted via
 * {@code GameQueryService.hasAuraWithEffect} from {@code isTransformPrevented}. Used by Bound by Moonsilver.
 */
public record EnchantedCreatureCantTransformEffect() implements CardEffect {
}
