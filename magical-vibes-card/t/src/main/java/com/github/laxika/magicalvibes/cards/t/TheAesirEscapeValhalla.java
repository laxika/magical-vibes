package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "39")
public class TheAesirEscapeValhalla extends Card {

    public TheAesirEscapeValhalla() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(
                ExileGraveyardCardsEffect.fromOwnGraveyardWithSource(1, new CardIsPermanentPredicate()),
                new GainLifeEffect(AmountThisWay.exiledManaValue())));

        addTrigger(Triggers.chapter(2), new PutCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new TotalManaValueOfCardsExiledWithSource()));
        setSagaChapterTargetGroups(2,
                List.of(new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));

        addTrigger(Triggers.chapter(3), SequenceEffect.of(
                ReturnToHandEffect.self(),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()));
    }
}
