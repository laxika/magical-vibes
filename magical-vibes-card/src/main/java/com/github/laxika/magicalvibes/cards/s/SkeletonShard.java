package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "242")
public class SkeletonShard extends Card {

    public SkeletonShard() {
        // {3}, {T} or {B}, {T}: Return target artifact creature card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(artifactCreaturePredicate())
                        .targetGraveyard(true)
                        .build()),
                "{3}, {T}: Return target artifact creature card from your graveyard to your hand."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(artifactCreaturePredicate())
                        .targetGraveyard(true)
                        .build()),
                "{B}, {T}: Return target artifact creature card from your graveyard to your hand."
        ));
    }

    private static CardAllOfPredicate artifactCreaturePredicate() {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)));
    }
}
