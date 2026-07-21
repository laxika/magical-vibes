package com.github.laxika.magicalvibes.model.effect;

/**
 * Static aura effect: "Enchanted permanent is a land" and loses all other card types.
 * Supertypes and subtypes (including existing land types) are retained. Applied in layer 4.
 * Used by Imprisoned in the Moon (paired with {@link BecomeColorlessEffect},
 * {@link LosesAllAbilitiesEffect}, and a granted {@code {T}: Add {C}} ability).
 */
public record EnchantedPermanentBecomesOnlyLandEffect() implements CardEffect {
}
