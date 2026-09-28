package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Puts a targeted nonlegendary creature card from the ability controller's graveyard onto the
 * battlefield under a targeted active opponent's control. The permanent gains haste and is goaded
 * until the controller's next turn, then exiled at the beginning of the next end step.
 */
public record PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect(
        int playerTargetGroup, int graveyardTargetGroup) implements CardEffect, TargetCardGroupEffect {

    public PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }

    @Override
    public List<Integer> targetGroups() {
        return List.of(graveyardTargetGroup);
    }
}
