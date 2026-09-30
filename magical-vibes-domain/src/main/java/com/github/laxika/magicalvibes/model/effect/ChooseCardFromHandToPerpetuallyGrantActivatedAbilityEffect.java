package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Chooses a matching card in the controller's hand and perpetually grants it an activated ability. */
public record ChooseCardFromHandToPerpetuallyGrantActivatedAbilityEffect(
        ActivatedAbility ability, CardPredicate cardFilter) implements CardEffect {

    public ChooseCardFromHandToPerpetuallyGrantActivatedAbilityEffect {
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(cardFilter, "cardFilter");
    }
}
