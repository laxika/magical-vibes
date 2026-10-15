package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "97")
public class DemonWall extends Card {

    public DemonWall() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.ANY),
                new CanAttackAsThoughNoDefenderEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(new PutCountersOnSourceEffect(1, 1, 2)),
                "{5}{B}: Put two +1/+1 counters on Demon Wall."
        ));
    }
}
