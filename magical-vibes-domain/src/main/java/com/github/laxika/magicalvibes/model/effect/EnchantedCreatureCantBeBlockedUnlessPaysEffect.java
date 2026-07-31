package com.github.laxika.magicalvibes.model.effect;

/**
 * Static aura effect: the enchanted creature can't be blocked unless the defending player pays
 * {@code amountPerBlocker} (generic mana) for each creature they control that's blocking it
 * (Awesome Presence — {3}). Summed per declared block at declare-blockers time via
 * {@code GameQueryService.getEnchantedCreatureBlockTax}; the block stays legal, only the cost gates it.
 */
public record EnchantedCreatureCantBeBlockedUnlessPaysEffect(int amountPerBlocker) implements CardEffect {
}
