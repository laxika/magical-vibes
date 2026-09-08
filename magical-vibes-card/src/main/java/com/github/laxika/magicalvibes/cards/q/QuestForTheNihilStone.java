package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "64")
public class QuestForTheNihilStone extends Card {

    public QuestForTheNihilStone() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for the Nihil Stone?"));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new ActivePlayerHandEmpty(),
                        new SourceCounterThreshold(2, CounterType.QUEST))),
                new MayEffect(new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER),
                        "Have that player lose 5 life?")));
    }
}
