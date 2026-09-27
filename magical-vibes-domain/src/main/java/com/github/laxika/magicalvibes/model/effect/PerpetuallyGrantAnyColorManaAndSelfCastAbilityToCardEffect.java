package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Objects;
import java.util.UUID;

/** Perpetually grants a card any-color casting permission and an ability that triggers when it is cast. */
public record PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(
        UUID cardId, CardEffect selfCastAbility) implements CardEffect, ChosenCardAwareEffect {

    public PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(CardEffect selfCastAbility) {
        this(null, selfCastAbility);
    }

    public PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect {
        Objects.requireNonNull(selfCastAbility, "selfCastAbility");
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(
                card.getId(), selfCastAbility);
    }
}
