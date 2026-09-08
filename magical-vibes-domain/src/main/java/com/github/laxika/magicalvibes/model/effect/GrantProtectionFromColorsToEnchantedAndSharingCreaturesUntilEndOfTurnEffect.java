package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Grants protection from the specified colors to the enchanted creature and every other creature
 * that shares a creature type with it until end of turn.
 */
public record GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect(
        Set<CardColor> colors) implements CardEffect {

    public GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect {
        colors = Set.copyOf(colors);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }
}
