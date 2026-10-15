package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "RIX", collectorNumber = "78")
public class MausoleumHarpy extends Card {

    public MausoleumHarpy() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ConditionalEffect(
                new ControllerHasCityBlessing(), new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
