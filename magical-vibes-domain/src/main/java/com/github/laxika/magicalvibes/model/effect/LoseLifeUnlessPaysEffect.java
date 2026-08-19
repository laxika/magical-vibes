package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher effect: target player loses N life unless they pay {M}.
 * Used by Isolation Cell and similar "pay mana or lose life" cards.
 * The affected player chooses whether to pay or take the life loss. When
 * {@code controllerGainsLifeLost} is true, the source's controller also gains the same amount
 * when the penalty is applied.
 *
 * @param lifeLoss    how much life the player loses if they don't pay
 * @param payAmount   generic mana cost the player can pay to avoid life loss
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 * @param controllerGainsLifeLost whether the source's controller gains the life lost when the
 *                                 payment is not made
 */
public record LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, CardPredicate spellFilter,
                                        boolean controllerGainsLifeLost) implements CardEffect {

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount) {
        this(lifeLoss, payAmount, null, false);
    }

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, CardPredicate spellFilter) {
        this(lifeLoss, payAmount, spellFilter, false);
    }

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, boolean controllerGainsLifeLost) {
        this(lifeLoss, payAmount, null, controllerGainsLifeLost);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
