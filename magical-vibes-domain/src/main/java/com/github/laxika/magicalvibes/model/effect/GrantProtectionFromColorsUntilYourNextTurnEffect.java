package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/** Grants a target creature protection from the specified colors until the effect controller's next turn. */
public record GrantProtectionFromColorsUntilYourNextTurnEffect(Set<CardColor> colors) implements CardEffect {

    public GrantProtectionFromColorsUntilYourNextTurnEffect {
        colors = Set.copyOf(colors);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
