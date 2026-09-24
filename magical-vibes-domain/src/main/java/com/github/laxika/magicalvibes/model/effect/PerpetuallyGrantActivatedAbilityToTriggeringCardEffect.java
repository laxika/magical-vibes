package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

import java.util.Objects;

/** Permanently grants an activated ability to the card that caused this trigger. */
public record PerpetuallyGrantActivatedAbilityToTriggeringCardEffect(ActivatedAbility ability)
        implements CardEffect {

    public PerpetuallyGrantActivatedAbilityToTriggeringCardEffect {
        Objects.requireNonNull(ability, "ability");
    }
}
