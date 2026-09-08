package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may exile a [card] from your graveyard. If you do, [thenEffect]."
 *
 * <p>The matching card is chosen at resolution. The follow-up effect remains part of the same
 * ability, so any target it declares is chosen as the ability is put on the stack.</p>
 */
public record ExileCardFromGraveyardThenEffect(
        CardPredicate filter,
        CardEffect thenEffect,
        String cardDescription
) implements CardEffect {

    public ExileCardFromGraveyardThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileCardFromGraveyardThenEffect requires a follow-up effect");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }
}
