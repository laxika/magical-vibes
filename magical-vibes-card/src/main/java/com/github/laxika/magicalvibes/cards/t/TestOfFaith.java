package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect;

@CardRegistration(set = "DST", collectorNumber = "17")
public class TestOfFaith extends Card {

    public TestOfFaith() {
        addEffect(EffectSlot.SPELL,
                new PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect(new Fixed(3)));
    }
}
