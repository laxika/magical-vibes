package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "39")
public class TheAesirEscapeValhalla extends Card {

    public TheAesirEscapeValhalla() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileOwnGraveyardCardThenEffect(
                new CardIsPermanentPredicate(),
                new GainLifeEffect(new TotalManaValueOfCardsExiledWithSource()),
                true,
                true));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new TotalManaValueOfCardsExiledWithSource()));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, SequenceEffect.of(
                ReturnToHandEffect.self(),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()));
    }
}
