package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability carried by a spell while it is on the stack: spells that target it cost
 * {@code amount} more to cast. E.g. Kaervek's Torch, amount = 2.
 *
 * <p>Read by {@code CastingCostService#getTargetingStackEntryTax} from the STATIC effects of the
 * card of every targeted stack entry. Applies to spells only (not activated abilities) and
 * symmetrically to both players.
 */
public record IncreaseCostOfSpellsTargetingThisSpellEffect(int amount) implements CardEffect {
}
