package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.UUID;

/** Returns any number of target creature cards from the controller's graveyard within a power cap. */
public record ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(int maxTotalPower,
        UUID dyingCardId)
        implements GraveyardCardChoosingEffect, DyingCreatureCardAwareEffect, DyingCreaturePermanentAwareEffect {

    private static final CardPredicate CREATURE_CARD = new CardTypePredicate(CardType.CREATURE);

    public ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(int maxTotalPower) {
        this(maxTotalPower, null);
    }

    public ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect() {
        this(0, null);
    }

    public ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect {
        if (maxTotalPower < 0) {
            throw new IllegalArgumentException("maxTotalPower cannot be negative");
        }
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return dyingCardId == null
                ? CREATURE_CARD
                : new CardAllOfPredicate(List.of(CREATURE_CARD, new CardNotPredicate(new CardIsSelfPredicate())));
    }

    @Override
    public Integer graveyardChoiceMaxTotalPower() {
        return maxTotalPower;
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(
                maxTotalPower, dyingCardId);
    }

    @Override
    public CardEffect boundToDyingCreature(Permanent dyingCreature) {
        return new ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(
                Math.max(0, dyingCreature.getEffectivePower()), dyingCardId);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                CREATURE_CARD, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
