package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "ECL", collectorNumber = "107")
public class HeirloomAuntie extends Card {

    public HeirloomAuntie() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new SurveilEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
