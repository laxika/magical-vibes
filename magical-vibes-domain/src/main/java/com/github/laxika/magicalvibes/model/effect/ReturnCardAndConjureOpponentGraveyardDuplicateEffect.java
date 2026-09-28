package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

/**
 * Returns a target card from the controller's graveyard to hand and, when kicked, conjures a
 * duplicate of a target card from an opponent's graveyard into hand.
 */
public record ReturnCardAndConjureOpponentGraveyardDuplicateEffect()
        implements IndependentlyTargetedGraveyardCardsEffect {

    private static final CardPredicate ANY_CARD = new CardTruePredicate();

    @Override
    public List<CardPredicate> targetFilters() {
        return List.of(ANY_CARD, ANY_CARD);
    }

    @Override
    public List<String> targetDescriptions() {
        return List.of("card", "card");
    }

    @Override
    public List<Integer> minimumTargetCounts() {
        return List.of(1, 0);
    }

    @Override
    public List<GraveyardSearchScope> targetScopes() {
        return List.of(GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                GraveyardSearchScope.OPPONENT_GRAVEYARD);
    }

    @Override
    public List<Boolean> targetGroupsOnlyWhenKicked() {
        return List.of(false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
