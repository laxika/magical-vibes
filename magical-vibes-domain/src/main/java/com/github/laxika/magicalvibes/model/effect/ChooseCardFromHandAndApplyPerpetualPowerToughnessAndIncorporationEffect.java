package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Chooses one hand card and perpetually changes its P/T and mana cost. */
public record ChooseCardFromHandAndApplyPerpetualPowerToughnessAndIncorporationEffect(
        CardPredicate cardFilter, int power, int toughness, String manaCost) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualPowerToughnessAndIncorporationEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(manaCost, "manaCost");
        if (manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
