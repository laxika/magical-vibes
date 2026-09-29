package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOrKeepOneAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "PIP", collectorNumber = "79")
@CardRegistration(set = "PIP", collectorNumber = "399")
@CardRegistration(set = "PIP", collectorNumber = "607")
@CardRegistration(set = "PIP", collectorNumber = "927")
public class LilyBowenRagingGrandma extends Card {

    public LilyBowenRagingGrandma() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DoublePlusOneCountersOrKeepOneAndGainLifeEffect(16));
    }
}
