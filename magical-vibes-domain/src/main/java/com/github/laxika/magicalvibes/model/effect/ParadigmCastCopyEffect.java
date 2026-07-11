package com.github.laxika.magicalvibes.model.effect;

/**
 * Paradigm (CR 702.192): at the beginning of each of the controller's precombat main phases,
 * create a copy of the spell in exile and offer a may-cast without paying its mana cost.
 * Resolved by {@code ParadigmCastCopyEffectHandler}.
 */
public record ParadigmCastCopyEffect() implements CardEffect {
}
