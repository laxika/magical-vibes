package com.github.laxika.magicalvibes.model.effect;

/**
 * Two target players each reveal the top card of their library, lose life equal to the mana value
 * of the card revealed by the other player, and put the card they revealed into their hand.
 */
public record TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), true, null, false, 2);
    }
}
