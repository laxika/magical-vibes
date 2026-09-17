package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "150")
public class ViashinoBranchrider extends Card {

    public ViashinoBranchrider() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{G}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}", List.of(new BoostSelfEffect(2, 0)),
                "{2}{R}: This creature gets +2/+0 until end of turn."));
    }
}
