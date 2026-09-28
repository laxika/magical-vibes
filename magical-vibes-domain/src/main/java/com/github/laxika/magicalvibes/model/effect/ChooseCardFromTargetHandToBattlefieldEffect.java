package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the effect controller put one matching card from a player's hand onto the battlefield.
 * The normal form targets the player and reveals their hand; the defending-player form reads the
 * defending player's hand from attack context.
 */
public record ChooseCardFromTargetHandToBattlefieldEffect(
        CardPredicate predicate, String label, boolean grantHaste, boolean sacrificeAtEndStep,
        boolean defendingPlayerHand, boolean enterTappedAndAttacking, boolean returnToHandAtEndStep)
        implements CardEffect {

    public ChooseCardFromTargetHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                       boolean grantHaste, boolean sacrificeAtEndStep) {
        this(predicate, label, grantHaste, sacrificeAtEndStep, false, false, false);
    }

    /** Non-targeting attack trigger that uses the defending player's hand. */
    public static ChooseCardFromTargetHandToBattlefieldEffect forDefendingPlayerHand(
            CardPredicate predicate, String label) {
        return new ChooseCardFromTargetHandToBattlefieldEffect(
                predicate, label, false, false, true, true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return defendingPlayerHand ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.player());
    }
}
