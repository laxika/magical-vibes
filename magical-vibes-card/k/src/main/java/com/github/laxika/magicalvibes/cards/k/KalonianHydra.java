package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "M14", collectorNumber = "181")
@CardRegistration(set = "MOC", collectorNumber = "305")
@CardRegistration(set = "BLC", collectorNumber = "226")
@CardRegistration(set = "C16", collectorNumber = "154")
@CardRegistration(set = "CM2", collectorNumber = "140")
public class KalonianHydra extends Card {

    public KalonianHydra() {
        // "This creature enters with four +1/+1 counters on it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        // "Whenever this creature attacks, double the number of +1/+1 counters on each creature you control."
        addEffect(EffectSlot.ON_ATTACK, new DoublePlusOneCountersOnControlledCreaturesEffect());
    }
}
