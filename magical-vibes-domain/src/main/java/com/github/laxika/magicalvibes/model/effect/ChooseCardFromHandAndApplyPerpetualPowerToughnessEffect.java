package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Chooses a matching card in the controller's hand and permanently changes its P/T. */
public record ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(
        CardPredicate cardFilter, int power, int toughness) implements CardEffect {
}
