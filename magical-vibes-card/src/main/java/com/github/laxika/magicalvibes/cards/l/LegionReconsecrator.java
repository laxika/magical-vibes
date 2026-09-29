package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardThenConjureSkeletonDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "11")
public class LegionReconsecrator extends Card {

    public LegionReconsecrator() {
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCreatureCardThenConjureSkeletonDuplicateEffect());

        CardPredicate powerOrToughnessOne = new CardAnyOfPredicate(List.of(
                new CardAllOfPredicate(List.of(
                        new CardPowerAtLeastPredicate(1),
                        new CardPowerAtMostPredicate(1))),
                new CardAllOfPredicate(List.of(
                        new CardToughnessAtLeastPredicate(1),
                        new CardToughnessAtMostPredicate(1)))));
        CardPredicate returnFilter = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardIsSelfPredicate()),
                powerOrToughnessOne));

        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(returnFilter)
                .targetGraveyard(true)
                .build());
    }
}
