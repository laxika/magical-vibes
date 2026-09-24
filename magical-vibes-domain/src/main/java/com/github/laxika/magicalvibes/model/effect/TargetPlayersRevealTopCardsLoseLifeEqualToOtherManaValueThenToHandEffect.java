package com.github.laxika.magicalvibes.model.effect;

/**
 * Two players each reveal the top card of their library, lose life equal to the mana value
 * of the card revealed by the other player, and put the card they revealed into their hand.
 * The controller may be one of those players implicitly, leaving only one declared target.
 */
public record TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect(
        boolean includeController) implements CardEffect {

    public TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), true, null, false, includeController ? 1 : 2);
    }
}
