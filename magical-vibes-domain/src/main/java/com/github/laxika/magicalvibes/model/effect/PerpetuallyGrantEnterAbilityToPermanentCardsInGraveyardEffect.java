package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Perpetually grants an enter-the-battlefield ability to permanent cards in a graveyard. */
public record PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect(
        CardEffect enterAbility) implements CardEffect {

    public PerpetuallyGrantEnterAbilityToPermanentCardsInGraveyardEffect {
        Objects.requireNonNull(enterAbility, "enterAbility");
    }
}
