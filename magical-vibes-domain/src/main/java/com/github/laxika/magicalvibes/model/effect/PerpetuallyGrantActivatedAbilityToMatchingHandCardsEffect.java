package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Perpetually grants an activated ability to matching cards currently in the controller's hand. */
public record PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect(
        CardPredicate cardFilter, ActivatedAbility ability) implements CardEffect {

    public PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(ability, "ability");
    }
}
