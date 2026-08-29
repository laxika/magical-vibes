package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals a card at random from their hand, then discards it unless they pay life
 * equal to one if it is a land card, or equal to its mana value otherwise. The target player makes
 * the pay-or-discard choice at resolution.
 */
public record RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
