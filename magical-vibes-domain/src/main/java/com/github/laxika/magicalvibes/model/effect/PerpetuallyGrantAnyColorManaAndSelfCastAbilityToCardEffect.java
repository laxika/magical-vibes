package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Perpetually grants the card a preceding choice selected (the stack entry's chosen card,
 * {@code StackEntry.getChosenObjectCard()}) any-color casting permission and an ability that triggers when it is cast.
 */
public record PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(CardEffect selfCastAbility)
        implements CardEffect {

    public PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect {
        Objects.requireNonNull(selfCastAbility, "selfCastAbility");
    }
}
