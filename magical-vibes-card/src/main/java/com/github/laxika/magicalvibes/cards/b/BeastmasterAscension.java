package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ZEN", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "182")
@CardRegistration(set = "SLD", collectorNumber = "1609")
@CardRegistration(set = "C14", collectorNumber = "186")
@CardRegistration(set = "C15", collectorNumber = "176")
@CardRegistration(set = "TLE", collectorNumber = "39")
public class BeastmasterAscension extends Card {

    public BeastmasterAscension() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new MayEffect(new PutCountersOnSourceCardEffect(CounterType.QUEST),
                        "Put a quest counter on Beastmaster Ascension?"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(7, CounterType.QUEST),
                new StaticBoostEffect(5, 5, GrantScope.OWN_CREATURES)));
    }
}
