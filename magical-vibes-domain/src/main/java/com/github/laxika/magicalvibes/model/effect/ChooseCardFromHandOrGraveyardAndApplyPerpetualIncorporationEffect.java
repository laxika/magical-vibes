package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Chooses a matching card in hand or graveyard and perpetually adds a cost and self-cast ability. */
public record ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect(
        CardPredicate cardFilter, String manaCost, CardEffect selfCastAbility) implements CardEffect {

    public ChooseCardFromHandOrGraveyardAndApplyPerpetualIncorporationEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(manaCost, "manaCost");
        Objects.requireNonNull(selfCastAbility, "selfCastAbility");
        if (manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
