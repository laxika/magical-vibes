package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Target creature gets +(basePower + count×powerPerCard)/+(baseToughness + count×toughnessPerCard)
 * until end of turn, where count is the number of matching cards in the controller's graveyard
 * at resolution time.
 *
 * <p>Used by Ancestral Anger with {@code CardNamedPredicate("Ancestral Anger")}, base power 1,
 * and power per card 1 for "X is 1 plus the number of cards named Ancestral Anger in your graveyard".</p>
 */
public record BoostTargetCreaturePerCardsInControllerGraveyardEffect(
        CardPredicate filter,
        int basePower,
        int powerPerCard,
        int baseToughness,
        int toughnessPerCard
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
