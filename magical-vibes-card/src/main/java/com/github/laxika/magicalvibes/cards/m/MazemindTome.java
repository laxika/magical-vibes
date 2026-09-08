package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "232")
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

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getCounterCount(CounterType.PAGE) >= 4,
                List.of(new ExileSelfThenEffect(new GainLifeEffect(4))),
                "Mazemind Tome's state-triggered ability"));
    }
}
