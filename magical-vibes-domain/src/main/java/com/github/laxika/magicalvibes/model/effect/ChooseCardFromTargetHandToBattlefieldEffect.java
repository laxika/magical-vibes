package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals a target player's hand and lets the effect controller put one matching card onto the
 * battlefield under their control.
 */
public record ChooseCardFromTargetHandToBattlefieldEffect(
        CardPredicate predicate, String label, boolean grantHaste, boolean sacrificeAtEndStep)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
