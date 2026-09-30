package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Chooses a matching hand card and perpetually adds a mana cost and self-cast ability to it. */
public record ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
        CardPredicate cardFilter, String manaCost, CardEffect selfCastAbility) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualIncorporationEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(manaCost, "manaCost");
        Objects.requireNonNull(selfCastAbility, "selfCastAbility");
        if (manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
