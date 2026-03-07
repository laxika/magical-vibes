package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher effect: target player loses N life unless they pay {M}.
 * Used by Isolation Cell and similar "pay mana or lose life" cards.
 * The affected player chooses whether to pay or take the life loss.
 *
 * @param lifeLoss    how much life the player loses if they don't pay
 * @param payAmount   generic mana cost the player can pay to avoid life loss
 * @param spellFilter optional filter for which spells trigger this (null = any spell)
 */
public record LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, CardPredicate spellFilter) implements CardEffect {

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount) {
        this(lifeLoss, payAmount, null);
    }
}
