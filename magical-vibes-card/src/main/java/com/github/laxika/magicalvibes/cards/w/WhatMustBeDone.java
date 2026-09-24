package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "11")
public class WhatMustBeDone extends Card {

    public WhatMustBeDone() {
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        var historicPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardIsHistoricPredicate()));
        var returnHistoricPermanent = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .filter(historicPermanent)
                .targetGraveyard(true)
                .plusOneCountersIfCardType(CardType.CREATURE)
                .plusOneCounterCount(2)
                .build();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Let the World Burn",
                        new DestroyAllPermanentsEffect(artifactOrCreature)),
                new ChooseOneEffect.ChooseOneOption(
                        "Release Juno",
                        returnHistoricPermanent,
                        new GraveyardCardPredicateTargetFilter(
                                historicPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
