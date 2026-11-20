package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "232")
@CardRegistration(set = "M21", collectorNumber = "383")
@CardRegistration(set = "BRR", collectorNumber = "30")
@CardRegistration(set = "FDC", collectorNumber = "268")
public class MazemindTome extends Card {

    public MazemindTome() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutTypedCounterOnSourceCost(CounterType.PAGE), new ScryEffect(1)),
                "{T}, Put a page counter on this artifact: Scry 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new PutTypedCounterOnSourceCost(CounterType.PAGE), new DrawCardEffect(1)),
                "{2}, {T}, Put a page counter on this artifact: Draw a card."
        ));

        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getCounterCount(CounterType.PAGE) >= 4),
                SequenceEffect.of(new ExileTargetPermanentEffect(Subjects.source()),
                        new ConditionalEffect(HappenedThisWay.exiled(), new GainLifeEffect(4))));
    }
}
