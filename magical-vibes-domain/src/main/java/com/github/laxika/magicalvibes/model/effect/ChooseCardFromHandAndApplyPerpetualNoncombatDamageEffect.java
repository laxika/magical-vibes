package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Chooses a matching card in hand and permanently increases its noncombat damage. */
public record ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect(
        CardPredicate cardFilter, int amount) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
