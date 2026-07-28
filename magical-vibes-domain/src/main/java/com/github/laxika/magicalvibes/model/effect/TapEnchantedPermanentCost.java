package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect for an Aura's activated ability whose cost is tapping the permanent it enchants
 * ("Tap enchanted land: ..." — Earthlore). The Aura itself is not tapped; the permanent named by
 * its {@code attachedTo} is. Because a tapped permanent cannot be tapped again, this cost also
 * models the "Activate only if enchanted land is untapped" clause those cards print.
 *
 * <p>Payment fires the enchanted-permanent-tap triggers (Psychic Venom, Spreading Algae), same as
 * any other way of tapping that permanent.
 */
public record TapEnchantedPermanentCost() implements CostEffect {
}
