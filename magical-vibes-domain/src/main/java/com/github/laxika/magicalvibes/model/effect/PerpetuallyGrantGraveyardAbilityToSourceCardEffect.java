package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.Objects;

/** Perpetually grants a graveyard-activated ability to the source card's identity. */
public record PerpetuallyGrantGraveyardAbilityToSourceCardEffect(ActivatedAbility ability)
        implements CardEffect {

    public PerpetuallyGrantGraveyardAbilityToSourceCardEffect {
        Objects.requireNonNull(ability, "ability");
    }
}
