package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.ManaValueOfLastCardExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "39")
public class TheAesirEscapeValhalla extends Card {

    public TheAesirEscapeValhalla() {
        addTrigger(Triggers.chapter(1), new ExileOwnGraveyardCardThenEffect(
                new CardIsPermanentPredicate(),
                new GainLifeEffect(new ManaValueOfLastCardExiledWithSource()),
                true,
                true,
                true));

        addTrigger(Triggers.chapter(2), new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new TotalManaValueOfCardsExiledWithSource()));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        addTrigger(Triggers.chapter(3), SequenceEffect.of(
                ReturnToHandEffect.self(),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()));
    }
}
