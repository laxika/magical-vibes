package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "251")
public class ContestedGameBall extends Card {

    public ContestedGameBall() {
        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                TriggerOptions.ONE_OR_MORE,
                new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true, true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DrawCardEffect(),
                        new PutCountersOnSelfEffect(CounterType.POINT),
                        new ConditionalEffect(
                                new SourceCounterThreshold(5, CounterType.POINT),
                                new SequenceEffect(List.of(new SacrificeSelfEffect(), CreateTokenEffect.ofTreasureToken(1))))
                ),
                "{2}, {T}: Draw a card and put a point counter on this artifact. Then if it has five or more point counters on it, sacrifice it and create a Treasure token."
        ));
    }
}
